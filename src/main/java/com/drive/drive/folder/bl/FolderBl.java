package com.drive.drive.folder.bl;

import com.drive.drive.audit.bl.NotificacionBl;
import com.drive.drive.file.bl.FileBl;
import com.drive.drive.file.dto.FileDto;
import com.drive.drive.file.entity.FileEntity;
import com.drive.drive.file.repository.FileRepository;
import com.drive.drive.folder.dto.FolderContentsDto;
import com.drive.drive.folder.dto.FolderDto;
import com.drive.drive.folder.entity.FolderEntity;
import com.drive.drive.folder.repository.FolderRepository;
import com.drive.drive.sharing.entity.SharedFolder;
import com.drive.drive.sharing.repository.SharedFolderRepository;
import com.drive.drive.user.dto.UsuarioDTO;
import com.drive.drive.user.entity.Usuario;
import com.drive.drive.user.repository.UsuarioRepository;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class FolderBl {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileBl fileBl;

    @Autowired
    private SharedFolderRepository sharedFolderRepository;

    @Autowired
    private NotificacionBl notificacionBl;

    // Método para listar las carpetas
    public List<String> listFolders() {
        try {
            List<Bucket> results = minioClient.listBuckets();
            List<String> folders = new java.util.ArrayList<>();
            for (Bucket result : results) {
                log.info(result.name());
                folders.add(result.name());
            }
            return folders;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void createFolder(String folderName, Long userId, Long parentFolderId) {
        try {
            folderName = folderName.toLowerCase();

            log.info("Creating folder: " + folderName);

            Usuario user = usuarioRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

            FolderEntity folder = new FolderEntity();
            Date date = new Date();

            folder.setCreationDate(date);
            folder.setAccessType("admin");
            folder.setUser(user);
            folder.setUpdateDate(date);
            folder.setDeleted(false);

            // Generate a unique folder name
            String uniqueFolderName = generateUniqueFolderName(folderName, parentFolderId, userId);

            // Replace spaces with hyphens in the folder name for consistent storage
            String formattedFolderName = uniqueFolderName.replace(" ", "-");

            folder.setName(formattedFolderName);

            if (parentFolderId != null) {
                FolderEntity parentFolder = folderRepository.findById(parentFolderId)
                        .orElseThrow(() -> new RuntimeException("Carpeta padre no encontrada con ID: " + parentFolderId));
                folder.setParentFolder(parentFolder);
            }

            folderRepository.save(folder);

            // Create bucket in MinIO with the formatted name
            createBucketIfNotExists(formattedFolderName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para verificar y crear un bucket si no existe
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

    // Método para generar un nombre de carpeta único
    private String generateUniqueFolderName(String folderName, Long parentFolderId, Long userId) {
        // Remover caracteres especiales y reemplazarlos con caracteres válidos
        String baseName = folderName.replaceAll("[^a-zA-Z0-9 ]", "");  // permitir espacios

        int counter = 0;
        boolean exists = true;

        while (exists) {
            String candidateName = baseName + (counter > 0 ? counter : "");
            exists = folderRepository.existsByNameAndParentFolder_IdAndUser_UsuarioID(candidateName, parentFolderId, userId);
            if (exists) {
                counter++;
            } else {
                folderName = candidateName;
            }
        }
        return folderName;
    }


    // ELiminar folder
    @Transactional
    public void deleteFolder(String folderName) {
        try {
            log.info("Deleting folder: " + folderName);
            String lowerCaseFolderName = folderName.toLowerCase();

            // Obtener todos los usuarios que tienen acceso a esta carpeta
            List<Long> userIds = sharedFolderRepository.findUserIdsByFolderName(lowerCaseFolderName);

            // Notificar a todos los usuarios que tienen acceso a la carpeta que será eliminada
            for (Long userId : userIds) {
                notificacionBl.crearNotificacionCompartir(userId,
                        "Carpeta Eliminada",
                        "La carpeta '" + lowerCaseFolderName + "' ha sido eliminada.",
                        "eliminada");
            }

            // Obtener las carpetas por nombre
            List<FolderEntity> folders = folderRepository.findByName(lowerCaseFolderName);
            if (folders.isEmpty()) {
                throw new RuntimeException("Carpeta no encontrada con nombre: " + lowerCaseFolderName);
            }

            for (FolderEntity folderEntity : folders) {
                Long folderEntityId = folderEntity.getId();

                // Eliminar carpetas hijas recursivamente
                deleteChildFolders(folderEntityId);

                // Eliminar referencias en carpetas_compartidos
                folderRepository.deleteSharedFolderReferencesByFolderId(folderEntityId);

                // Eliminar los documentos que referencian esta carpeta
                folderRepository.deleteDocumentsByFolderId(folderEntityId);

                // Actualizar las carpetas que tienen esta carpeta como carpeta padre, estableciendo parentFolder a null
                folderRepository.clearParentFolderReferences(folderEntityId);

                // Eliminar todos los objetos dentro del bucket
                Iterable<Result<Item>> objects = minioClient.listObjects(
                        ListObjectsArgs.builder().bucket(lowerCaseFolderName).build()
                );
                for (Result<Item> object : objects) {
                    String objectName = object.get().objectName();
                    minioClient.removeObject(
                            RemoveObjectArgs.builder()
                                    .bucket(lowerCaseFolderName)
                                    .object(objectName)
                                    .build()
                    );
                    log.info("Deleted object: " + objectName);
                }

                // Eliminar la carpeta de MinIO
                RemoveBucketArgs removeBucketArgs = RemoveBucketArgs.builder()
                        .bucket(lowerCaseFolderName)
                        .build();
                minioClient.removeBucket(removeBucketArgs);

                // Eliminar la carpeta de la base de datos
                folderRepository.deleteById(folderEntityId);
                log.info("Folder '{}' deleted successfully.", lowerCaseFolderName);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error deleting folder '{}': {}", folderName, e.getMessage());
            throw new RuntimeException("Error deleting folder: " + e.getMessage());
        }
    }

    // Eliminar folders hijos
    @Transactional
    public void deleteChildFolders(Long parentFolderId) {
        List<FolderEntity> childFolders = folderRepository.findByParentFolder_Id(parentFolderId);
        for (FolderEntity childFolder : childFolders) {
            // Recursivamente eliminar documentos y carpetas hijas
            deleteChildFolders(childFolder.getId());

            // Eliminar referencias en carpetas_compartidos de carpetas hijas
            folderRepository.deleteSharedFolderReferencesByFolderId(childFolder.getId());

            folderRepository.deleteDocumentsByFolderId(childFolder.getId());
            folderRepository.deleteById(childFolder.getId());
        }
    }


    // listar folders por usuario
    public List<FolderDto> listFoldersByUser(Long userId) {
        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        // Obtiene todas las carpetas asociadas al usuario
        List<FolderEntity> folders = folderRepository.findByUser(user);

        // Filtra solo las carpetas padre (donde parentFolder es null)
        List<FolderEntity> parentFolders = folders.stream()
                .filter(folder -> folder.getParentFolder() == null)
                .collect(Collectors.toList());

        // Convierte las entidades de carpetas padre a DTOs
        return parentFolders.stream().map(this::convertToDto).collect(Collectors.toList());
    }


    // conevertir los datos a dto
    private FolderDto convertToDto(FolderEntity folderEntity) {
        FolderDto folderDto = new FolderDto();
        folderDto.setId(folderEntity.getId());
        folderDto.setName(folderEntity.getName());
        folderDto.setAccessType(folderEntity.getAccessType());
        folderDto.setCreationDate(folderEntity.getCreationDate());
        folderDto.setUpdateDate(folderEntity.getUpdateDate());
        folderDto.setDeleted(folderEntity.getDeleted());
        folderDto.setUserId(folderEntity.getUser().getUsuarioID());
        folderDto.setParentFolderId(folderEntity.getParentFolder() != null ? folderEntity.getParentFolder().getId() : null);
        folderDto.setParentFolderId(folderEntity.getParentFolder() != null ? folderEntity.getParentFolder().getId() : null);

        return folderDto;
    }

    public void shareFolder(Long folderId, Long emisorId, Long receptorId) {
        Usuario emisor = usuarioRepository.findById(emisorId)
                .orElseThrow(() -> new RuntimeException("Usuario emisor no encontrado con ID: " + emisorId));
        Usuario receptor = usuarioRepository.findById(receptorId)
                .orElseThrow(() -> new RuntimeException("Usuario receptor no encontrado con ID: " + receptorId));
        FolderEntity folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Carpeta no encontrada con ID: " + folderId));

        SharedFolder sharedFolder = new SharedFolder();
        sharedFolder.setFolder(folder);
        sharedFolder.setEmisor(emisor);
        sharedFolder.setReceptor(receptor);
        sharedFolder.setSharedAt(new Date());

        sharedFolderRepository.save(sharedFolder);

        // Notificar al receptor sobre la carpeta compartida
        notificacionBl.crearNotificacionCompartir(receptorId,
                "Carpeta Compartida",
                "Has recibido acceso a la carpeta '" + folder.getName() + "' de " + emisor.getNombres() + " " + emisor.getPaterno(),
                "compartida");
    }


    //
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


    // Método para listar las carpetas compartidas de un usuario
    public List<FolderDto> listSharedFolders(Long userId) {
        Map<Long, FolderDto> uniqueFolders = new HashMap<>();

        // Procesar carpetas recibidas
        List<SharedFolder> receivedFolders = sharedFolderRepository.findByReceptor_UsuarioID(userId);
        receivedFolders.forEach(sharedFolder ->
                uniqueFolders.put(sharedFolder.getFolder().getId(), convertToDto(sharedFolder.getFolder())));

        // Procesar carpetas enviadas
        List<SharedFolder> sentFolders = sharedFolderRepository.findByEmisor_UsuarioID(userId);
        sentFolders.forEach(sharedFolder ->
                uniqueFolders.put(sharedFolder.getFolder().getId(), convertToDto(sharedFolder.getFolder())));

        // Devolver solo los valores del mapa, que son los FolderDto únicos
        return new ArrayList<>(uniqueFolders.values());
    }


    // Método para listar los contenidos de una carpeta compartida
    public FolderContentsDto listSharedFolderContents(Long userId, Long folderId) {
        boolean isSharedWithUser = sharedFolderRepository.findByReceptor_UsuarioIDAndFolder_Id(userId, folderId).stream().anyMatch(sf -> sf.getFolder().getId().equals(folderId));
        boolean isSharedByUser = sharedFolderRepository.findByEmisor_UsuarioIDAndFolder_Id(userId, folderId).stream().anyMatch(sf -> sf.getFolder().getId().equals(folderId));

        if (isSharedWithUser || isSharedByUser) {
            List<FileEntity> files = fileRepository.findByFolder_IdAndDeletedFalse(folderId);
            List<FolderEntity> subFolders = folderRepository.findByParentFolder_IdAndDeletedFalse(folderId);
            List<FileDto> fileDtos = files.stream().map(file -> {
                FileDto dto = new FileDto();
                dto.setId(file.getId());
                dto.setTitle(file.getTitle());
                dto.setDescription(file.getDescription());
                dto.setEtag(file.getEtag());
                dto.setAccessType(file.getAccessType());
                dto.setPassword(file.getPassword());
                dto.setCategoria(file.getCategoria());
                dto.setCreatedDate(file.getCreatedDate());
                dto.setModifiedDate(file.getModifiedDate());
                // Aquí asumimos la existencia de un método que genera URLs de descarga; si no existe, se debe implementar
                dto.setMinioLink(fileBl.getDownloadUrl(file.getEtag(), file.getFolder().getName()));
                return dto;
            }).collect(Collectors.toList());

            List<FolderDto> folderDtos = subFolders.stream().map(folder -> {
                FolderDto dto = new FolderDto();
                dto.setId(folder.getId());
                dto.setName(folder.getName());
                dto.setAccessType(folder.getAccessType());
                dto.setCreationDate(folder.getCreationDate());
                dto.setUpdateDate(folder.getUpdateDate());
                dto.setUserId(folder.getUser().getUsuarioID());
                dto.setParentFolderId(folder.getParentFolder().getId());
                return dto;
            }).collect(Collectors.toList());
            return new FolderContentsDto(folderDtos, fileDtos);
        } else {
            throw new RuntimeException("La carpeta no ha sido compartida con este usuario o no existe.");
        }
    }

    // Método para listar los usuarios con acceso a una carpeta
    public List<UsuarioDTO> listUsersWithAccessToFolder(Long folderId) {
        List<SharedFolder> sharedFolders = sharedFolderRepository.findByFolder_Id(folderId);
        List<UsuarioDTO> usersWithAccess = new ArrayList<>();

        for (SharedFolder sharedFolder : sharedFolders) {
            Usuario receptor = sharedFolder.getReceptor();
            UsuarioDTO dto = new UsuarioDTO();
            dto.setUsuarioID(receptor.getUsuarioID());
            dto.setIdServidor(receptor.getIdServidor());
            dto.setUsuario(receptor.getUsuario());
            dto.setCi(receptor.getCi());
            dto.setNombres(receptor.getNombres());
            dto.setPaterno(receptor.getPaterno());
            dto.setMaterno(receptor.getMaterno());
            dto.setCelular(receptor.getCelular());
            usersWithAccess.add(dto);
        }

        return usersWithAccess.stream().distinct().collect(Collectors.toList()); // Eliminar duplicados si fuera necesario
    }

    // comartir con toso los usaurios
    public void shareFolderWithAllUsers(Long folderId, Long emisorId) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        FolderEntity folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Carpeta no encontrada con ID: " + folderId));
        for (Usuario usuario : usuarios) {
            if (!usuario.getUsuarioID().equals(emisorId)) {
                shareFolder(folderId, emisorId, usuario.getUsuarioID());
                notificacionBl.crearNotificacionCompartir(usuario.getUsuarioID(),
                        "Nueva Carpeta Compartida",
                        "Tienes acceso a una nueva carpeta: '" + folder.getName() + "' compartida por " + usuarioRepository.findById(emisorId).get().getNombres(),
                        "compartida");
            }
        }
    }


    // En la clase FolderBl
    public void shareFolderWithUsersByDependency(String dependencyName, Long folderId, Long emisorId) {
        // Obtener la carpeta que se va a compartir
        FolderEntity folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Carpeta no encontrada con ID: " + folderId));

        // Verificar que exista la dependencia
        List<Usuario> usuariosEnDependencia = usuarioRepository.findByDependencia(dependencyName);
        if (usuariosEnDependencia.isEmpty()) {
            throw new RuntimeException("No existen usuarios en la dependencia: " + dependencyName);
        }

        // Obtener el usuario emisor
        Usuario emisor = usuarioRepository.findById(emisorId)
                .orElseThrow(() -> new RuntimeException("Usuario emisor no encontrado con ID: " + emisorId));

        // Compartir la carpeta con cada usuario de la dependencia
        for (Usuario receptor : usuariosEnDependencia) {
            if (!receptor.getUsuarioID().equals(emisorId)) { // Asegurarse de no compartir la carpeta con uno mismo
                shareFolder(folderId, emisorId, receptor.getUsuarioID());
                // Crear notificaciones para cada receptor que recibe acceso a la carpeta
                notificacionBl.crearNotificacionCompartir(receptor.getUsuarioID(),
                        "Carpeta Compartida",
                        "Tienes acceso a una nueva carpeta: '" + folder.getName() + "' compartida por " + emisor.getNombres(),
                        "compartida");
            }
        }
    }

    // generar el link de folder
    public List<String> generateSharedFolderLinks(Long folderId, Long userId) {
        List<String> links = new ArrayList<>();
        try {
            FolderEntity folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new RuntimeException("Carpeta no encontrada con ID: " + folderId));

            // Generar enlaces para los archivos en la carpeta actual
            links.addAll(generateLinksForFolder(folder));

            // Recorrer las subcarpetas y generar enlaces recursivamente
            List<FolderEntity> subfolders = folderRepository.findByParentFolder_IdAndDeletedFalse(folderId);
            for (FolderEntity subfolder : subfolders) {
                links.addAll(generateSharedFolderLinks(subfolder.getId(), userId));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al generar enlaces para la carpeta: " + e.getMessage(), e);
        }
        return links;
    }

    // gerear el link de los folders
    private List<String> generateLinksForFolder(FolderEntity folder) {
        List<String> links = new ArrayList<>();
        try {
            String bucketName = folder.getName();
            System.out.println("Bucket Name: " + bucketName);

            Iterable<Result<Item>> items = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .build());

            for (Result<Item> result : items) {
                Item item = result.get();
                System.out.println("Found item: " + item.objectName());

                String presignedUrl = minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.GET)
                                .bucket(bucketName)
                                .object(item.objectName())
                                .expiry(7, TimeUnit.DAYS)
                                .build());
                links.add(presignedUrl);
                System.out.println("Generated URL: " + presignedUrl);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al generar enlaces para la carpeta: " + e.getMessage(), e);
        }
        return links;
    }

    // generar el link de los folders por deppendencia
    public List<FolderDto> getSharedFoldersByDependency(String dependencyName) {
        // Obtener las carpetas compartidas con usuarios de la dependencia especificada
        List<SharedFolder> sharedFolders = sharedFolderRepository.findByReceptorDependencia(dependencyName);

        // Convertir las carpetas compartidas a DTOs
        Set<FolderDto> folderDtos = sharedFolders.stream()
                .map(sharedFolder -> convertToDtos(sharedFolder.getFolder()))
                .collect(Collectors.toSet());

        return new ArrayList<>(folderDtos);
    }

    // conversion a dto de los datos json
    private FolderDto convertToDtos(FolderEntity folder) {
        FolderDto folderDto = new FolderDto();
        folderDto.setId(folder.getId());
        folderDto.setName(folder.getName());
        folderDto.setAccessType(folder.getAccessType());
        folderDto.setCreationDate(folder.getCreationDate());
        folderDto.setUpdateDate(folder.getUpdateDate());
        folderDto.setDeleted(folder.getDeleted());
        folderDto.setUserId(folder.getUser().getUsuarioID());
        folderDto.setParentFolderId(folder.getParentFolder() != null ? folder.getParentFolder().getId() : null);
        return folderDto;
    }



    // Método para obtener carpetas compartidas con un usuario específico
    public List<FolderDto> getSharedFoldersWithUser(Long userId) {
        List<SharedFolder> sharedFoldersByUser = sharedFolderRepository.findByReceptor_UsuarioID(userId);
        Set<FolderDto> sharedFolders = new HashSet<>();

        sharedFoldersByUser.forEach(sharedFolder -> sharedFolders.add(convertToDto(sharedFolder.getFolder())));

        return new ArrayList<>(sharedFolders);
    }

    // Método para descargar el contenido de un bucket
    public void downloadBucketContents(String bucketName, OutputStream outputStream) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(outputStream);
        try {
            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).build()
            );

            for (Result<Item> objectResult : objects) {
                Item item = objectResult.get();
                InputStream stream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucketName)
                                .object(item.objectName())
                                .build()
                );

                ZipEntry zipEntry = new ZipEntry(item.objectName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = stream.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                stream.close();
                zipOut.closeEntry();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to download bucket: " + e.getMessage(), e);
        } finally {
            zipOut.close();
        }
    }
}
