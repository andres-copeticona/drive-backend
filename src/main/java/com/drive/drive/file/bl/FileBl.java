package com.drive.drive.file.bl;


import com.drive.drive.audit.bl.NotificacionBl;
import com.drive.drive.file.dto.FileDto;
import com.drive.drive.file.entity.FileEntity;
import com.drive.drive.file.repository.FileRepository;
import com.drive.drive.folder.dto.FolderContentsDto;
import com.drive.drive.folder.dto.FolderDto;
import com.drive.drive.folder.entity.FolderEntity;
import com.drive.drive.folder.repository.FolderRepository;
import com.drive.drive.sharing.dto.SharedDocumentDto;
import com.drive.drive.sharing.entity.SharedDocumentEntity;
import com.drive.drive.sharing.repository.SharingRepository;
import com.drive.drive.user.entity.Usuario;
import com.drive.drive.user.repository.UsuarioRepository;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FileBl
{
    @Autowired
    private MinioClient minioClient;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SharingRepository sharingRepository;

    @Autowired
    private NotificacionBl notificacionBl;

    // Método para subir un archivo
    public FileDto uploadFile(FileDto fileDto, MultipartFile file) {
        try {
            FileEntity fileNuevo = new FileEntity();

            FolderEntity folder = folderRepository.findById(fileDto.getFolderId()).orElseThrow(() -> new RuntimeException("Folder not found"));
            Usuario usuario = usuarioRepository.findById(fileDto.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
            Date date = new Date();

            String originalFileName = file.getOriginalFilename();
            String uniqueFileName = generateUniqueFileName(originalFileName);

            fileNuevo.setFolder(folder);
            fileNuevo.setUser(usuario);
            fileNuevo.setTitle(originalFileName);
            fileNuevo.setDeleted(false);
            fileNuevo.setCreatedDate(date);
            fileNuevo.setModifiedDate(date);
            fileNuevo.setCategoria(fileDto.getCategoria());
            fileNuevo.setAccessType(fileDto.getAccessType());
            fileNuevo.setPassword(fileDto.getPassword());
            fileNuevo.setDescription(fileDto.getDescription());
            fileNuevo.setSize(file.getSize());
            fileNuevo.setFileType(determineFileType(originalFileName));

            createBucketIfNotExists(folder.getName());

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(folder.getName())
                            .object(uniqueFileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build()
            );

            fileNuevo.setMinioLink(folder.getName() + "/" + uniqueFileName);

            String etag = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(folder.getName())
                            .object(uniqueFileName)
                            .build()
            ).etag();

            fileNuevo.setEtag(etag);
            fileRepository.save(fileNuevo);

            // Actualizar FileDto con el ID y el enlace MinIO
            fileDto.setId(fileNuevo.getId());
            fileDto.setMinioLink(fileNuevo.getMinioLink());
            fileDto.setEtag(fileNuevo.getEtag());

            return fileDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error uploading file: " + e.getMessage());
        }
    }

    // Método para generar un nombre de archivo único
    private String generateUniqueFileName(String originalName) {
        String fileExtension = originalName.substring(originalName.lastIndexOf('.'));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return originalName.substring(0, originalName.lastIndexOf('.')) + "_" + sdf.format(new Date()) + fileExtension;
    }

    // Método para crear un bucket si no existe
    private void createBucketIfNotExists(String bucketName) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (MinioException e) {
            e.printStackTrace();
            throw new RuntimeException("Error checking or creating bucket: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error checking or creating bucket: " + e.getMessage());
        }
    }

    // Método para determinar el tipo de archivo
    public String determineFileType(String fileName) {
        if (fileName.endsWith(".pdf")) {
            return "PDF";
        } else if (fileName.matches("(?i).*(\\.jpeg|\\.jpg|\\.png|\\.gif)$")) {
            return "Imagen";
        } else if (fileName.matches("(?i).*(\\.mp4|\\.avi|\\.mov)$")) {
            return "Video";
        } else if (fileName.matches("(?i).*(\\.mp3|\\.wav)$")) {
            return "Música";
        }
        return "Otro";
    }

    // Método para listar todos los archivos
    public List<FileDto> listAllFiles() {
        List<FileEntity> files = fileRepository.findByDeletedFalse();
        return files.stream().map(file -> {
            FileDto dto = new FileDto();
            dto.setId(file.getId());
            dto.setUserId(file.getUser().getUsuarioID());
            dto.setTitle(file.getTitle());
            dto.setDescription(file.getDescription());
            dto.setEtag(file.getEtag());
            dto.setAccessType(file.getAccessType());
            dto.setPassword(file.getPassword());
            dto.setCategoria(file.getCategoria());
            dto.setSize(file.getSize());
            dto.setCreatedDate(file.getCreatedDate());
            dto.setModifiedDate(file.getModifiedDate());
            dto.setDeleted(file.getDeleted());
            dto.setMinioLink(getDownloadUrl(file.getEtag(), file.getFolder().getName()));
            // Añade aquí más mapeos si hay más campos en FileDto
            return dto;
        }).collect(Collectors.toList());
    }

    // Método para obtener la URL de descarga de un archivo
    public String getDownloadUrl(String etag, String bucket)
    {
        try {
            String bucketName = bucket;

            Iterable<Result<Item>> myObjects = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            for (Result<Item> result : myObjects) {
                Item item = result.get();
                String etag1 = item.etag().substring(1, item.etag().length() - 1);

                if (etag1.equals(etag)) {
                    return minioClient.getPresignedObjectUrl(
                            GetPresignedObjectUrlArgs.builder()
                                    .method(Method.GET)
                                    .bucket(bucketName)
                                    .object(item.objectName())
                                    .expiry(1, TimeUnit.HOURS)
                                    .build());
                }
            }
            return "No se encontro el archivo";
        } catch (MinioException e) {
            e.printStackTrace();
            return "Error getting file URL: " + e.getMessage();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Método para compartir un archivo
    public String shareFile(SharedDocumentDto sharedDocumentDto) {
        FileEntity file = fileRepository.findById(sharedDocumentDto.getDocumentoId())
                .orElseThrow(() -> new RuntimeException("File not found"));

        Usuario userToShareWith = usuarioRepository.findById(sharedDocumentDto.getReceptorUsuarioId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Usuario userEmisor = usuarioRepository.findById(sharedDocumentDto.getEmisorUsuarioId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        SharedDocumentEntity sharedDocument = new SharedDocumentEntity();
        sharedDocument.setDocumento(file);
        sharedDocument.setReceptor(userToShareWith);
        sharedDocument.setEmisor(userEmisor);
        sharedDocument.setCreatedAt(new Date());
        sharedDocument.setTipoAcceso(sharedDocumentDto.getTipoAcceso());
        sharedDocument.setLinkDocumento(getDownloadUrl(file.getEtag(), file.getFolder().getName()));
        sharingRepository.save(sharedDocument);

        // Crear notificaciones
        notificacionBl.crearNotificacionCompartir(sharedDocumentDto.getEmisorUsuarioId(),
                "Documento Compartido",
                "Has compartido el documento '" + file.getTitle() + "' con " + userToShareWith.getNombres() + " " + userToShareWith.getPaterno(),
                "compartido");

        notificacionBl.crearNotificacionCompartir(sharedDocumentDto.getReceptorUsuarioId(),
                "Documento Recibido",
                "Has recibido un documento compartido '" + file.getTitle() + "' de " + userEmisor.getNombres() + " " + userEmisor.getPaterno(),
                "compartido");

        return getDownloadUrl(file.getEtag(), file.getFolder().getName());
    }


    //mostrar los documento compartidos por id de usaurio
    public List<SharedDocumentDto> findAllSharedDocumentsByUserId(Long userId) {
        List<SharedDocumentEntity> emisorDocuments = sharingRepository.findByEmisor_UsuarioID(userId);
        List<SharedDocumentEntity> receptorDocuments = sharingRepository.findByReceptor_UsuarioID(userId);

        Set<SharedDocumentEntity> allDocuments = new HashSet<>();
        allDocuments.addAll(emisorDocuments);
        allDocuments.addAll(receptorDocuments);

        return allDocuments.stream().map(sharedDocument -> {
            SharedDocumentDto dto = new SharedDocumentDto();
            dto.setCompartidoId(sharedDocument.getCompartidoId());
            dto.setDocumentoId(sharedDocument.getDocumento().getId());
            dto.setReceptorUsuarioId(sharedDocument.getReceptor().getUsuarioID());
            dto.setEmisorUsuarioId(sharedDocument.getEmisor().getUsuarioID());
            dto.setTipoAcceso(sharedDocument.getTipoAcceso());
            dto.setCreatedAt(sharedDocument.getCreatedAt());
            dto.setLinkDocumento(sharedDocument.getLinkDocumento());
            dto.setNombreDocumento(sharedDocument.getDocumento().getTitle());

            // Obtener el nombre del emisor y receptor por ID
            String emisorNombre = usuarioRepository.findById(sharedDocument.getEmisor().getUsuarioID()).get().getNombres();
            String receptorNombre = usuarioRepository.findById(sharedDocument.getReceptor().getUsuarioID()).get().getNombres();

            // Establecer el nombre del emisor y receptor en el DTO
            dto.setEmisorNombre(emisorNombre);
            dto.setReceptorNombre(receptorNombre);

            return dto;
        }).collect(Collectors.toList());
    }


    // Método para listar los archivos por usuario y carpeta
    public FolderContentsDto listFilesByUserAndFolder(Long userId, Long folderId) {
        // Sólo incluir archivos y carpetas no eliminados
        List<FileEntity> files = fileRepository.findByUser_UsuarioIDAndFolder_IdAndDeletedFalse(userId, folderId);
        List<FolderEntity> subFolders = folderRepository.findByParentFolder_IdAndDeletedFalse(folderId);

        List<FileDto> fileDtos = files.stream().map(file -> {
            FileDto dto = new FileDto();
            dto.setId(file.getId());
            dto.setTitle(file.getTitle());
            dto.setDescription(file.getDescription());
            dto.setEtag(file.getEtag());
            dto.setAccessType(file.getAccessType());
            dto.setSize(file.getSize());
            dto.setPassword(file.getPassword());
            dto.setCreatedDate(file.getCreatedDate());
            dto.setModifiedDate(file.getModifiedDate());
            // No es necesario setear deleted porque solo incluimos no eliminados
            dto.setCategoria(file.getCategoria());
            dto.setMinioLink(getDownloadUrl(file.getEtag(), file.getFolder().getName()));
            if (file.getUser() != null) {
                dto.setUserId(file.getUser().getUsuarioID());
            }
            if (file.getFolder() != null) {
                dto.setFolderId(file.getFolder().getId());
            }
            return dto;
        }).collect(Collectors.toList());

        List<FolderDto> folderDtos = subFolders.stream().map(folder -> {
            FolderDto dto = new FolderDto();
            dto.setId(folder.getId());
            dto.setName(folder.getName());
            dto.setAccessType(folder.getAccessType());
            dto.setCreationDate(folder.getCreationDate());
            dto.setUpdateDate(folder.getUpdateDate());
            // No es necesario setear deleted porque solo incluimos no eliminados
            if (folder.getUser() != null) {
                dto.setUserId(folder.getUser().getUsuarioID());
            }
            if (folder.getParentFolder() != null) {
                dto.setParentFolderId(folder.getParentFolder().getId());
            }
            return dto;
        }).collect(Collectors.toList());

        return new FolderContentsDto(folderDtos, fileDtos);
    }


    // Método para obtener un archivo por ID
    public FileDto getFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .map(file -> {
                    FileDto dto = new FileDto();
                    dto.setId(file.getId());
                    dto.setTitle(file.getTitle());
                    dto.setDescription(file.getDescription());
                    dto.setEtag(file.getEtag());
                    dto.setAccessType(file.getAccessType());
                    dto.setPassword(file.getPassword());
                    dto.setCategoria(file.getCategoria());
                    dto.setSize(file.getSize());
                    dto.setCreatedDate(file.getCreatedDate());
                    dto.setModifiedDate(file.getModifiedDate());
                    dto.setDeleted(file.getDeleted());
                    dto.setMinioLink(getDownloadUrl(file.getEtag(), file.getFolder().getName()));
                    if (file.getUser() != null) {
                        dto.setUserId(file.getUser().getUsuarioID());
                    }
                    if (file.getFolder() != null) {
                        dto.setFolderId(file.getFolder().getId());
                    }
                    return dto;
                }).orElse(null); // Retorna null si no se encuentra el archivo
    }

    // Método para encontrar todos los archivos públicos
    public List<FileDto> findAllPublicFiles() {
        List<FileEntity> files = fileRepository.findByAccessTypeAndDeletedFalse("publico");
        return files.stream().map(file -> {
            FileDto dto = new FileDto();
            dto.setId(file.getId());
            dto.setTitle(file.getTitle());
            dto.setDescription(file.getDescription());
            dto.setEtag(file.getEtag());
            dto.setAccessType(file.getAccessType());
            dto.setPassword(file.getPassword());
            dto.setSize(file.getSize());
            dto.setCategoria(file.getCategoria());
            dto.setCreatedDate(file.getCreatedDate());
            dto.setModifiedDate(file.getModifiedDate());
            dto.setDeleted(file.getDeleted());
            dto.setMinioLink(getDownloadUrl(file.getEtag(), file.getFolder().getName()));
            if (file.getUser() != null) {
                dto.setUserId(file.getUser().getUsuarioID());
            }
            if (file.getFolder() != null) {
                dto.setFolderId(file.getFolder().getId());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    // Método para eliminar un archivo
    public String deleteFile(Long fileId) {
        try {
            // Primero, obtén una lista de todos los usuarios que tienen acceso al archivo para notificarlos
            List<SharedDocumentEntity> sharedDocuments = sharingRepository.findByDocumento_Id(fileId);
            List<Long> userIds = sharedDocuments.stream()
                    .map(doc -> doc.getReceptor().getUsuarioID())
                    .distinct()
                    .collect(Collectors.toList());

            FileEntity file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            // Notificar a todos los usuarios que tenían acceso antes de eliminar
            String titulo = "Documento Eliminado";
            String mensaje = "El documento '" + file.getTitle() + "' ha sido eliminado.";
            for (Long userId : userIds) {
                notificacionBl.crearNotificacionCompartir(userId, titulo, mensaje, "eliminado");
            }

            // Elimina todas las referencias compartidas
            deleteSharedReferences(fileId);

            String bucket = file.getFolder().getName();
            String originalFileName = file.getTitle(); // Este es el nombre con el que se guardó en la base de datos

            System.out.println("Bucket: " + bucket);
            System.out.println("Original FileName: " + originalFileName);

            minioClient.removeObject(
                    io.minio.RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(originalFileName)
                            .build()
            );

            fileRepository.delete(file);

            return "Archivo eliminado correctamente.";
        } catch (MinioException e) {
            e.printStackTrace();
            return "Error al eliminar el archivo en MinIO: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al eliminar el archivo: " + e.getMessage();
        }
    }

    // Método para eliminar un archivo por ID
    public void deleteSharedReferences(Long fileId) {
        List<SharedDocumentEntity> sharedDocuments = sharingRepository.findByDocumento_Id(fileId);
        if (!sharedDocuments.isEmpty()) {
            sharingRepository.deleteAll(sharedDocuments);
        }
    }

    // Método para encontrar los archivos recientes por ID de usuario
    public List<FileDto> findRecentFilesByUserId(Long userId) {
        // Encuentra los archivos por usuario, que no estén eliminados y ordenados por fecha de creación
        List<FileEntity> recentFiles = fileRepository.findByUser_UsuarioIDAndDeletedFalseOrderByCreatedDateDesc(userId);

        // Convierte los FileEntity a FileDto
        return recentFiles.stream().map(file -> {
            FileDto dto = new FileDto();
            dto.setId(file.getId());
            dto.setUserId(file.getUser().getUsuarioID());
            dto.setTitle(file.getTitle());
            dto.setDescription(file.getDescription());
            dto.setEtag(file.getEtag());
            dto.setAccessType(file.getAccessType());
            dto.setFolderId(file.getFolder().getId());
            dto.setSize(file.getSize());
            dto.setPassword(file.getPassword());
            dto.setCategoria(file.getCategoria());
            dto.setCreatedDate(file.getCreatedDate());
            dto.setModifiedDate(file.getModifiedDate());
            dto.setDeleted(file.getDeleted());
            dto.setMinioLink(getDownloadUrl(file.getEtag(), file.getFolder().getName()));
            // Añade aquí más mapeos si hay más campos en FileDto
            return dto;
        }).collect(Collectors.toList());
    }

    // Método para encontrar los archivos por categoría y usuario
    public List<FileDto> findFilesByCategoryAndUser(String categoria, Long userId) {
        List<FileEntity> files = fileRepository.findByCategoriaAndUser_UsuarioIDAndDeletedFalse(categoria, userId);
        return files.stream().map(this::mapToFileDto).collect(Collectors.toList());
    }

    // Método para mapear un FileEntity a un FileDto
    private FileDto mapToFileDto(FileEntity file) {
        FileDto dto = new FileDto();
        dto.setId(file.getId());
        dto.setUserId(file.getUser().getUsuarioID());
        dto.setTitle(file.getTitle());
        dto.setDescription(file.getDescription());
        dto.setEtag(file.getEtag());
        dto.setAccessType(file.getAccessType());
        dto.setPassword(file.getPassword());
        dto.setSize(file.getSize());
        dto.setCategoria(file.getCategoria());
        dto.setCreatedDate(file.getCreatedDate());
        dto.setModifiedDate(file.getModifiedDate());
        dto.setDeleted(file.getDeleted());
        dto.setMinioLink(getDownloadUrl(file.getEtag(), file.getFolder().getName()));
        if (file.getFolder() != null) {
            dto.setFolderId(file.getFolder().getId());
        }
        // Añade más mapeos según sea necesario.
        return dto;
    }

    // Actualiza la categoría de un archivo
    public String updateFileCategory(Long fileId, String newCategory) {
        if (!newCategory.equals("Nuevo") && !newCategory.equals("Reemplazado") && !newCategory.equals("Sellado")) {
            throw new IllegalArgumentException("Categoría inválida. Las categorías válidas son: Nuevo, Reemplazado, Sellado.");
        }

        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado con ID: " + fileId));

        file.setCategoria(newCategory);
        file.setModifiedDate(new Date()); // Actualiza la fecha de modificación del archivo
        fileRepository.save(file);

        return "La categoría del archivo ha sido actualizada a: " + newCategory;
    }

    public Map<String, Long> countCategoriesByUser(Long userId) {
        List<FileEntity> files = fileRepository.findByUser_UsuarioIDAndDeletedFalse(userId);
        Map<String, Long> categoryCounts = files.stream()
                .collect(Collectors.groupingBy(file -> file.getCategoria() == null ? "Sin categoría" : file.getCategoria(), Collectors.counting()));

        // Asegúrate de incluir todas las categorías en el mapa, incluso si no tienen archivos.
        String[] categories = new String[] {"Nuevo", "Reemplazado", "Sellado", "Sin categoría"};
        for (String category : categories) {
            categoryCounts.putIfAbsent(category, 0L);
        }

        return categoryCounts;
    }


    public Map<String, List<String>> findAllSharedDocumentsUsersByUserId(Long userId) {
        List<SharedDocumentEntity> sharedDocumentsAsEmisor = sharingRepository.findByEmisor_UsuarioID(userId);
        List<SharedDocumentEntity> sharedDocumentsAsReceptor = sharingRepository.findByReceptor_UsuarioID(userId);

        Set<String> emisorNames = new HashSet<>();
        Set<String> receptorNames = new HashSet<>();

        for (SharedDocumentEntity sharedDocument : sharedDocumentsAsEmisor) {
            Usuario receptor = sharedDocument.getReceptor();
            receptorNames.add(receptor.getNombres()); // Asume que 'getNombres()' retorna el nombre del usuario
        }

        for (SharedDocumentEntity sharedDocument : sharedDocumentsAsReceptor) {
            Usuario emisor = sharedDocument.getEmisor();
            emisorNames.add(emisor.getNombres());
        }

        Map<String, List<String>> result = new HashMap<>();
        result.put("sharedWithMe", new ArrayList<>(emisorNames));
        result.put("iSharedWith", new ArrayList<>(receptorNames));

        return result;
    }

    public Map<String, Object> getFileStatsByUserId(Long userId) {
        List<FileEntity> files = fileRepository.findByUser_UsuarioIDAndDeletedFalse(userId);
        long totalSize = 0L;
        Map<String, Long> countByFileType = new HashMap<>();
        Map<String, Double> sizeByFileType = new HashMap<>();

        for (FileEntity file : files) {
            String fileType = file.getFileType();  // Asegúrate de que este método retorna el tipo de archivo correctamente
            Long fileSize = (file.getSize() == null) ? 0L : file.getSize();
            totalSize += fileSize;
            countByFileType.merge(fileType, 1L, Long::sum);
            sizeByFileType.merge(fileType, (double) fileSize, Double::sum);
        }

        Map<String, String> sizeByFileTypeFormatted = new HashMap<>();
        for (Map.Entry<String, Double> entry : sizeByFileType.entrySet()) {
            sizeByFileTypeFormatted.put(entry.getKey(), convertSizeToReadableFormat(entry.getValue()));
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("TotalSize", convertSizeToReadableFormat(totalSize));
        stats.put("CountByFileType", countByFileType);
        stats.put("SizeByFileType", sizeByFileTypeFormatted);

        return stats;
    }


    private String convertSizeToReadableFormat(double size) {
        final double KB = 1024.0;
        final double MB = KB * 1024;
        final double GB = MB * 1024;

        if (size < KB) return String.format("%.0f B", size);
        if (size < MB) return String.format("%.2f KB", size / KB);
        if (size < GB) return String.format("%.2f MB", size / MB);
        return String.format("%.2f GB", size / GB);
    }

    public Map<String, Object> getTotalStorageUsedByUser(Long userId) {
        return getFileStatsByUserId(userId);
    }



    public List<SharedDocumentDto> findSharedDocumentsBetweenUsers(Long emisorId, Long receptorId) {
        List<SharedDocumentEntity> sharedDocuments = sharingRepository.findSharedDocumentsBetweenUsers(emisorId, receptorId);
        return sharedDocuments.stream().map(document -> {
            SharedDocumentDto dto = new SharedDocumentDto();
            dto.setCompartidoId(document.getCompartidoId());
            dto.setDocumentoId(document.getDocumento().getId());
            dto.setReceptorUsuarioId(document.getReceptor().getUsuarioID());
            dto.setEmisorUsuarioId(document.getEmisor().getUsuarioID());
            dto.setTipoAcceso(document.getTipoAcceso());
            dto.setCreatedAt(document.getCreatedAt());
            dto.setLinkDocumento(getDownloadUrl(document.getDocumento().getEtag(), document.getDocumento().getFolder().getName()));
            dto.setNombreDocumento(document.getDocumento().getTitle());
            dto.setCategoria(document.getDocumento().getCategoria());
            // Obtener y establecer los nombres de emisor y receptor si es necesario
            return dto;
        }).collect(Collectors.toList());
    }
}