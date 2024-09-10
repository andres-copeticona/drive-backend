package com.drive.drive.modules.file.services;

import com.drive.drive.modules.file.dto.CreateFilesDto;
import com.drive.drive.modules.file.dto.FileDto;
import com.drive.drive.modules.file.dto.FileFilter;
import com.drive.drive.modules.file.dto.FileValidator;
import com.drive.drive.modules.file.entities.FileEntity;
import com.drive.drive.modules.file.mappers.FileMapper;
import com.drive.drive.modules.file.repositories.FileRepository;
import com.drive.drive.modules.folder.entities.FolderEntity;
import com.drive.drive.modules.folder.repositories.FolderRepository;
import com.drive.drive.security.UserData;
import com.drive.drive.shared.services.MinioService;
import com.drive.drive.shared.services.NotificationService;
import com.drive.drive.sharing.entity.SharedDocumentEntity;
import com.drive.drive.sharing.repository.SharingRepository;

import lombok.extern.slf4j.Slf4j;

import com.drive.drive.shared.dto.DownloadDto;
import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.ResponseDto;
import com.drive.drive.modules.user.entities.UserEntity;
import com.drive.drive.modules.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileService {
  @Autowired
  private FileRepository fileRepository;

  @Autowired
  private FolderRepository folderRepository;

  @Autowired
  private SharingRepository sharingRepository;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private MinioService minioService;

  public ResponseDto<ListResponseDto<List<FileDto>>> uploadMultipleFiles(UserData userData,
      CreateFilesDto createFilesDto) {
    try {
      FileValidator fileValidator = new FileValidator();
      Arrays.asList(createFilesDto.getFiles()).forEach(file -> {
        if (!fileValidator.isValidFile(file))
          throw new IllegalArgumentException("Invalid file type. Only JPG, JPEG, PDF, MP4, and MP3 files are allowed.");
      });

      FolderEntity folder = folderRepository.findById(createFilesDto.getFolderId()).get();
      UserEntity user = userRepository.findById(userData.getUserId()).get();
      List<FileDto> fileDtos = new ArrayList<>();

      for (MultipartFile file : createFilesDto.getFiles()) {
        FileEntity fileEntity = FileMapper.createFilesDtoToFileEntity(createFilesDto, file, folder);
        fileEntity.setUser(user);
        fileDtos.add(FileMapper.FileEntityToDto(fileRepository.save(fileEntity)));
        minioService.addObject(fileEntity, folder, file);
      }

      return new ResponseDto<>(201, new ListResponseDto<List<FileDto>>(fileDtos, Long.valueOf(fileDtos.size())),
          "Archivos cargados correctamente");
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseDto<>(500, null, "Error al cargar los archivos");
    }
  }

  public ResponseDto<ListResponseDto<List<FileDto>>> listAllFiles(FileFilter filter) {
    try {
      Specification<FileEntity> spec = filter.getSpecification();
      Sort sort = filter.getSort();
      Pageable pageable = filter.getPageable();

      List<FileEntity> files;
      Long total = 0L;

      if (pageable == null) {
        files = fileRepository.findAll(spec, sort);
        total = Long.valueOf(files.size());
      } else {
        var res = fileRepository.findAll(spec, pageable);
        files = res.getContent();
        total = res.getTotalElements();
      }

      List<FileDto> dtos = files.stream().map(FileMapper::FileEntityToDto).collect(Collectors.toList());
      return new ResponseDto<>(200, new ListResponseDto<>(dtos, total), "Lista de archivos");
    } catch (Exception e) {
      log.error("Error listing files: {}", e.getMessage());
      return new ResponseDto<>(500, null, "Error al listar los archivos");
    }
  }

  public ResponseDto<FileDto> getFileById(Long fileId) {
    ResponseDto<FileDto> res = new ResponseDto<FileDto>().setCode(200);
    try {
      FileEntity file = fileRepository.findById(fileId).get();
      FileDto dto = FileMapper.FileEntityToDto(file);
      dto.setLink(minioService.getDownloadUrl(file.getFolder().getCode(), file.getCode()));

      return res.setData(dto).setMessage("Archivo obtenido correctamente");
    } catch (Exception e) {
      log.error("Error getting file by ID {}: {}", fileId, e.getMessage());
      return res.setCode(500).setMessage("Error al obtener el archivo");
    }
  }

  public DownloadDto download(Long fileId) {
    try {
      FileEntity file = fileRepository.findById(fileId).get();
      byte[] data = minioService.download(file.getFolder().getCode(), file.getCode()).readAllBytes();
      return new DownloadDto(file.getTitle(), data);
    } catch (Exception e) {
      log.error("Error downloading file by ID {}: {}", fileId, e.getMessage());
      return null;
    }
  }

  // Método para compartir un archivo
  // public String shareFile(SharedDocumentDto sharedDocumentDto) {
  // FileEntity file = fileRepository.findById(sharedDocumentDto.getDocumentoId())
  // .orElseThrow(() -> new RuntimeException("File not found"));
  //
  // UserEntity userToShareWith =
  // usuarioRepository.findById(sharedDocumentDto.getReceptorUsuarioId())
  // .orElseThrow(() -> new RuntimeException("User not found"));
  //
  // UserEntity userEmisor =
  // usuarioRepository.findById(sharedDocumentDto.getEmisorUsuarioId())
  // .orElseThrow(() -> new RuntimeException("User not found"));
  //
  // SharedDocumentEntity sharedDocument = new SharedDocumentEntity();
  // sharedDocument.setDocumento(file);
  // sharedDocument.setReceptor(userToShareWith);
  // sharedDocument.setEmisor(userEmisor);
  // sharedDocument.setCreatedAt(new Date());
  // sharedDocument.setTipoAcceso(sharedDocumentDto.getTipoAcceso());
  // sharedDocument.setLinkDocumento(minioService.getDownloadUrl(file.getEtag(),
  // file.getFolder().getName()));
  // sharingRepository.save(sharedDocument);
  //
  // // Crear notificaciones
  // notificacionBl.crearNotificacionCompartir(sharedDocumentDto.getEmisorUsuarioId(),
  // "Documento Compartido",
  // "Has compartido el documento '" + file.getTitle() + "' con " +
  // userToShareWith.getNombres() + " "
  // + userToShareWith.getPaterno(),
  // "compartido");
  //
  // notificacionBl.crearNotificacionCompartir(sharedDocumentDto.getReceptorUsuarioId(),
  // "Documento Recibido",
  // "Has recibido un documento compartido '" + file.getTitle() + "' de " +
  // userEmisor.getNombres() + " "
  // + userEmisor.getPaterno(),
  // "compartido");
  //
  // return minioService.getDownloadUrl(file.getEtag(),
  // file.getFolder().getName());
  // }
  //
  // // mostrar los documento compartidos por id de usaurio
  // public List<SharedDocumentDto> findAllSharedDocumentsByUserId(Long userId) {
  // List<SharedDocumentEntity> emisorDocuments =
  // sharingRepository.findByEmisor_UsuarioID(userId);
  // List<SharedDocumentEntity> receptorDocuments =
  // sharingRepository.findByReceptor_UsuarioID(userId);
  //
  // Set<SharedDocumentEntity> allDocuments = new HashSet<>();
  // allDocuments.addAll(emisorDocuments);
  // allDocuments.addAll(receptorDocuments);
  //
  // return allDocuments.stream().map(sharedDocument -> {
  // SharedDocumentDto dto = new SharedDocumentDto();
  // dto.setCompartidoId(sharedDocument.getCompartidoId());
  // dto.setDocumentoId(sharedDocument.getDocumento().getId());
  // dto.setReceptorUsuarioId(sharedDocument.getReceptor().getUsuarioID());
  // dto.setEmisorUsuarioId(sharedDocument.getEmisor().getUsuarioID());
  // dto.setTipoAcceso(sharedDocument.getTipoAcceso());
  // dto.setCreatedAt(sharedDocument.getCreatedAt());
  // dto.setLinkDocumento(sharedDocument.getLinkDocumento());
  // dto.setNombreDocumento(sharedDocument.getDocumento().getTitle());
  //
  // // Obtener el nombre del emisor y receptor por ID
  // String emisorNombre =
  // usuarioRepository.findById(sharedDocument.getEmisor().getUsuarioID()).get().getNombres();
  // String receptorNombre =
  // usuarioRepository.findById(sharedDocument.getReceptor().getUsuarioID()).get()
  // .getNombres();
  //
  // // Establecer el nombre del emisor y receptor en el DTO
  // dto.setEmisorNombre(emisorNombre);
  // dto.setReceptorNombre(receptorNombre);
  //
  // return dto;
  // }).collect(Collectors.toList());
  // }
  //
  // // Método para listar los archivos por usuario y carpeta
  // public FolderContentsDto listFilesByUserAndFolder(Long userId, Long folderId)
  // {
  // // Sólo incluir archivos y carpetas no eliminados
  // List<FileEntity> files =
  // fileRepository.findByUser_UsuarioIDAndFolder_IdAndDeletedFalse(userId,
  // folderId);
  // List<FolderEntity> subFolders =
  // folderRepository.findByParentFolder_IdAndDeletedFalse(folderId);
  //
  // List<FileDto> fileDtos = files.stream().map(file -> {
  // FileDto dto = new FileDto();
  // dto.setId(file.getId());
  // dto.setTitle(file.getTitle());
  // dto.setDescription(file.getDescription());
  // dto.setEtag(file.getEtag());
  // dto.setAccessType(file.getAccessType());
  // dto.setSize(file.getSize());
  // dto.setPassword(file.getPassword());
  // dto.setCreatedDate(file.getCreatedDate());
  // dto.setModifiedDate(file.getModifiedDate());
  // // No es necesario setear deleted porque solo incluimos no eliminados
  // dto.setCategoria(file.getCategoria());
  // dto.setMinioLink(minioService.getDownloadUrl(file.getFolder().getCode(),
  // file.getCode()));
  // if (file.getUser() != null) {
  // dto.setUserId(file.getUser().getUsuarioID());
  // }
  // if (file.getFolder() != null) {
  // dto.setFolderId(file.getFolder().getId());
  // }
  // return dto;
  // }).collect(Collectors.toList());
  //
  // List<FolderDto> folderDtos = subFolders.stream().map(folder -> {
  // FolderDto dto = new FolderDto();
  // dto.setId(folder.getId());
  // dto.setName(folder.getName());
  // dto.setAccessType(folder.getAccessType());
  // dto.setCreationDate(folder.getCreationDate());
  // dto.setUpdateDate(folder.getUpdateDate());
  // // No es necesario setear deleted porque solo incluimos no eliminados
  // if (folder.getUser() != null) {
  // dto.setUserId(folder.getUser().getUsuarioID());
  // }
  // if (folder.getParentFolder() != null) {
  // dto.setParentFolderId(folder.getParentFolder().getId());
  // }
  // return dto;
  // }).collect(Collectors.toList());
  //
  // return new FolderContentsDto(folderDtos, fileDtos);
  // }
  //
  // // Método para obtener un archivo por ID
  // public FileDto getFileById(Long fileId) {
  // return fileRepository.findById(fileId)
  // .map(file -> {
  // FileDto dto = new FileDto();
  // dto.setId(file.getId());
  // dto.setTitle(file.getTitle());
  // dto.setDescription(file.getDescription());
  // dto.setEtag(file.getEtag());
  // dto.setAccessType(file.getAccessType());
  // dto.setPassword(file.getPassword());
  // dto.setCategoria(file.getCategoria());
  // dto.setSize(file.getSize());
  // dto.setCreatedDate(file.getCreatedDate());
  // dto.setModifiedDate(file.getModifiedDate());
  // dto.setDeleted(file.getDeleted());
  // dto.setMinioLink(minioService.getDownloadUrl(file.getEtag(),
  // file.getFolder().getName()));
  // if (file.getUser() != null) {
  // dto.setUserId(file.getUser().getUsuarioID());
  // }
  // if (file.getFolder() != null) {
  // dto.setFolderId(file.getFolder().getId());
  // }
  // return dto;
  // }).orElse(null); // Retorna null si no se encuentra el archivo
  // }
  //
  // // Método para encontrar todos los archivos públicos
  // public List<FileDto> findAllPublicFiles() {
  // List<FileEntity> files =
  // fileRepository.findByAccessTypeAndDeletedFalse("publico");
  // return files.stream().map(file -> {
  // FileDto dto = new FileDto();
  // dto.setId(file.getId());
  // dto.setTitle(file.getTitle());
  // dto.setDescription(file.getDescription());
  // dto.setEtag(file.getEtag());
  // dto.setAccessType(file.getAccessType());
  // dto.setPassword(file.getPassword());
  // dto.setSize(file.getSize());
  // dto.setCategoria(file.getCategoria());
  // dto.setCreatedDate(file.getCreatedDate());
  // dto.setModifiedDate(file.getModifiedDate());
  // dto.setDeleted(file.getDeleted());
  // dto.setMinioLink(minioService.getDownloadUrl(file.getEtag(),
  // file.getFolder().getName()));
  // if (file.getUser() != null) {
  // dto.setUserId(file.getUser().getUsuarioID());
  // }
  // if (file.getFolder() != null) {
  // dto.setFolderId(file.getFolder().getId());
  // }
  // return dto;
  // }).collect(Collectors.toList());
  // }
  //
  public ResponseDto<Boolean> deleteFile(Long fileId) {
    try {
      FileEntity file = fileRepository.findById(fileId)
          .orElseThrow(() -> new RuntimeException("File not found"));

      List<SharedDocumentEntity> sharedDocuments = sharingRepository.findByDocumento_Id(fileId);

      this.notificationService.sendDeleteFileNotification(sharedDocuments, file);

      sharingRepository.deleteAll(sharedDocuments);

      if (!sharedDocuments.isEmpty())
        sharingRepository.deleteAll(sharedDocuments);

      minioService.deleteObject(file);

      fileRepository.delete(file);

      return new ResponseDto<>(200, true, "Archivo eliminado correctamente");
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseDto<>(500, false, "Error al eliminar el archivo");
    }
  }
  //
  // // Método para encontrar los archivos recientes por ID de usuario
  // public List<FileDto> findRecentFilesByUserId(Long userId) {
  // // Encuentra los archivos por usuario, que no estén eliminados y ordenados
  // por
  // // fecha de creación
  // List<FileEntity> recentFiles =
  // fileRepository.findByUser_UsuarioIDAndDeletedFalseOrderByCreatedDateDesc(userId);
  //
  // // Convierte los FileEntity a FileDto
  // return recentFiles.stream().map(file -> {
  // FileDto dto = new FileDto();
  // dto.setId(file.getId());
  // dto.setUserId(file.getUser().getUsuarioID());
  // dto.setTitle(file.getTitle());
  // dto.setDescription(file.getDescription());
  // dto.setEtag(file.getEtag());
  // dto.setAccessType(file.getAccessType());
  // dto.setFolderId(file.getFolder().getId());
  // dto.setSize(file.getSize());
  // dto.setPassword(file.getPassword());
  // dto.setCategoria(file.getCategoria());
  // dto.setCreatedDate(file.getCreatedDate());
  // dto.setModifiedDate(file.getModifiedDate());
  // dto.setDeleted(file.getDeleted());
  // dto.setMinioLink(minioService.getDownloadUrl(file.getEtag(),
  // file.getFolder().getName()));
  // // Añade aquí más mapeos si hay más campos en FileDto
  // return dto;
  // }).collect(Collectors.toList());
  // }
  //
  // // Método para encontrar los archivos por categoría y usuario
  // public List<FileDto> findFilesByCategoryAndUser(String categoria, Long
  // userId) {
  // List<FileEntity> files =
  // fileRepository.findByCategoriaAndUser_UsuarioIDAndDeletedFalse(categoria,
  // userId);
  // return files.stream().map(this::mapToFileDto).collect(Collectors.toList());
  // }
  //
  // // Método para mapear un FileEntity a un FileDto
  // private FileDto mapToFileDto(FileEntity file) {
  // FileDto dto = new FileDto();
  // dto.setId(file.getId());
  // dto.setUserId(file.getUser().getUsuarioID());
  // dto.setTitle(file.getTitle());
  // dto.setDescription(file.getDescription());
  // dto.setEtag(file.getEtag());
  // dto.setAccessType(file.getAccessType());
  // dto.setPassword(file.getPassword());
  // dto.setSize(file.getSize());
  // dto.setCategoria(file.getCategoria());
  // dto.setCreatedDate(file.getCreatedDate());
  // dto.setModifiedDate(file.getModifiedDate());
  // dto.setDeleted(file.getDeleted());
  // dto.setMinioLink(minioService.getDownloadUrl(file.getEtag(),
  // file.getFolder().getName()));
  // if (file.getFolder() != null) {
  // dto.setFolderId(file.getFolder().getId());
  // }
  // // Añade más mapeos según sea necesario.
  // return dto;
  // }
  //
  // // Actualiza la categoría de un archivo
  // public String updateFileCategory(Long fileId, String newCategory) {
  // if (!newCategory.equals("Nuevo") && !newCategory.equals("Reemplazado") &&
  // !newCategory.equals("Sellado")) {
  // throw new IllegalArgumentException(
  // "Categoría inválida. Las categorías válidas son: Nuevo, Reemplazado,
  // Sellado.");
  // }
  //
  // FileEntity file = fileRepository.findById(fileId)
  // .orElseThrow(() -> new RuntimeException("Archivo no encontrado con ID: " +
  // fileId));
  //
  // file.setCategoria(newCategory);
  // file.setModifiedDate(new Date()); // Actualiza la fecha de modificación del
  // archivo
  // fileRepository.save(file);
  //
  // return "La categoría del archivo ha sido actualizada a: " + newCategory;
  // }
  //
  // public Map<String, Long> countCategoriesByUser(Long userId) {
  // List<FileEntity> files =
  // fileRepository.findByUser_UsuarioIDAndDeletedFalse(userId);
  // Map<String, Long> categoryCounts = files.stream()
  // .collect(Collectors.groupingBy(file -> file.getCategoria() == null ? "Sin
  // categoría" : file.getCategoria(),
  // Collectors.counting()));
  //
  // // Asegúrate de incluir todas las categorías en el mapa, incluso si no tienen
  // // archivos.
  // String[] categories = new String[] { "Nuevo", "Reemplazado", "Sellado", "Sin
  // categoría" };
  // for (String category : categories) {
  // categoryCounts.putIfAbsent(category, 0L);
  // }
  //
  // return categoryCounts;
  // }
  //
  // public Map<String, List<String>> findAllSharedDocumentsUsersByUserId(Long
  // userId) {
  // List<SharedDocumentEntity> sharedDocumentsAsEmisor =
  // sharingRepository.findByEmisor_UsuarioID(userId);
  // List<SharedDocumentEntity> sharedDocumentsAsReceptor =
  // sharingRepository.findByReceptor_UsuarioID(userId);
  //
  // Set<String> emisorNames = new HashSet<>();
  // Set<String> receptorNames = new HashSet<>();
  //
  // for (SharedDocumentEntity sharedDocument : sharedDocumentsAsEmisor) {
  // UserEntity receptor = sharedDocument.getReceptor();
  // receptorNames.add(receptor.getNombres()); // Asume que 'getNombres()' retorna
  // el nombre del usuario
  // }
  //
  // for (SharedDocumentEntity sharedDocument : sharedDocumentsAsReceptor) {
  // UserEntity emisor = sharedDocument.getEmisor();
  // emisorNames.add(emisor.getNombres());
  // }
  //
  // Map<String, List<String>> result = new HashMap<>();
  // result.put("sharedWithMe", new ArrayList<>(emisorNames));
  // result.put("iSharedWith", new ArrayList<>(receptorNames));
  //
  // return result;
  // }
  //
  // public Map<String, Object> getFileStatsByUserId(Long userId) {
  // List<FileEntity> files =
  // fileRepository.findByUser_UsuarioIDAndDeletedFalse(userId);
  // long totalSize = 0L;
  // Map<String, Long> countByFileType = new HashMap<>();
  // Map<String, Double> sizeByFileType = new HashMap<>();
  //
  // for (FileEntity file : files) {
  // String fileType = file.getFileType(); // Asegúrate de que este método retorna
  // el tipo de archivo correctamente
  // Long fileSize = (file.getSize() == null) ? 0L : file.getSize();
  // totalSize += fileSize;
  // countByFileType.merge(fileType, 1L, Long::sum);
  // sizeByFileType.merge(fileType, (double) fileSize, Double::sum);
  // }
  //
  // Map<String, String> sizeByFileTypeFormatted = new HashMap<>();
  // for (Map.Entry<String, Double> entry : sizeByFileType.entrySet()) {
  // sizeByFileTypeFormatted.put(entry.getKey(),
  // convertSizeToReadableFormat(entry.getValue()));
  // }
  //
  // Map<String, Object> stats = new HashMap<>();
  // stats.put("TotalSize", convertSizeToReadableFormat(totalSize));
  // stats.put("CountByFileType", countByFileType);
  // stats.put("SizeByFileType", sizeByFileTypeFormatted);
  //
  // return stats;
  // }
  //
  // private String convertSizeToReadableFormat(double size) {
  // final double KB = 1024.0;
  // final double MB = KB * 1024;
  // final double GB = MB * 1024;
  //
  // if (size < KB)
  // return String.format("%.0f B", size);
  // if (size < MB)
  // return String.format("%.2f KB", size / KB);
  // if (size < GB)
  // return String.format("%.2f MB", size / MB);
  // return String.format("%.2f GB", size / GB);
  // }
  //
  // public Map<String, Object> getTotalStorageUsedByUser(Long userId) {
  // return getFileStatsByUserId(userId);
  // }
  //
  // public List<SharedDocumentDto> findSharedDocumentsBetweenUsers(Long emisorId,
  // Long receptorId) {
  // List<SharedDocumentEntity> sharedDocuments =
  // sharingRepository.findSharedDocumentsBetweenUsers(emisorId,
  // receptorId);
  // return sharedDocuments.stream().map(document -> {
  // SharedDocumentDto dto = new SharedDocumentDto();
  // dto.setCompartidoId(document.getCompartidoId());
  // dto.setDocumentoId(document.getDocumento().getId());
  // dto.setReceptorUsuarioId(document.getReceptor().getUsuarioID());
  // dto.setEmisorUsuarioId(document.getEmisor().getUsuarioID());
  // dto.setTipoAcceso(document.getTipoAcceso());
  // dto.setCreatedAt(document.getCreatedAt());
  // dto.setLinkDocumento(
  // minioService.getDownloadUrl(document.getDocumento().getEtag(),
  // document.getDocumento().getFolder().getName()));
  // dto.setNombreDocumento(document.getDocumento().getTitle());
  // dto.setCategoria(document.getDocumento().getCategoria());
  // // Obtener y establecer los nombres de emisor y receptor si es necesario
  // return dto;
  // }).collect(Collectors.toList());
  // }
}
