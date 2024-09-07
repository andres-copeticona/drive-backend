package com.drive.drive.modules.folder.services;

import com.drive.drive.audit.bl.NotificacionBl;
import com.drive.drive.modules.file.dto.FileDto;
import com.drive.drive.modules.file.entities.FileEntity;
import com.drive.drive.modules.file.repositories.FileRepository;
import com.drive.drive.modules.folder.dto.CreateFolderDto;
import com.drive.drive.modules.folder.dto.FolderContentsDto;
import com.drive.drive.modules.folder.dto.FolderDto;
import com.drive.drive.modules.folder.dto.FolderFilter;
import com.drive.drive.modules.folder.entities.FolderEntity;
import com.drive.drive.modules.folder.mappers.FolderMapper;
import com.drive.drive.modules.folder.repositories.FolderRepository;
import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.ResponseDto;
import com.drive.drive.shared.services.MinioService;
import com.drive.drive.shared.services.NotificationService;
import com.drive.drive.sharing.entity.SharedFolder;
import com.drive.drive.sharing.repository.SharedFolderRepository;
import com.drive.drive.modules.user.dto.UserDto;
import com.drive.drive.modules.user.entities.UserEntity;
import com.drive.drive.modules.user.mappers.UserMapper;
import com.drive.drive.modules.user.repositories.UserRepository;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import io.minio.messages.Item;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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
public class FolderService {

  @Autowired
  private MinioClient minioClient;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private FolderRepository folderRepository;

  @Autowired
  private UserRepository usuarioRepository;

  @Autowired
  private FileRepository fileRepository;

  @Autowired
  private MinioService minioService;

  @Autowired
  private SharedFolderRepository sharedFolderRepository;

  @Autowired
  private NotificacionBl notificacionBl;

  public ResponseDto<ListResponseDto<List<FolderDto>>> listFolders(FolderFilter filter) {
    try {
      Specification<FolderEntity> spec = filter.getSpecification();
      Sort sort = filter.getSort();
      Pageable pageable = filter.getPageable();

      List<FolderEntity> folders;
      Long total = 0L;

      if (pageable == null) {
        folders = folderRepository.findAll(spec, sort);
        total = Long.valueOf(folders.size());
      } else {
        var res = folderRepository.findAll(spec, pageable);
        folders = res.getContent();
        total = res.getTotalElements();
      }

      List<FolderDto> dtos = folders.stream().map(FolderMapper::entityToDto).collect(Collectors.toList());
      return new ResponseDto<>(200, new ListResponseDto<>(dtos, total), "Lista de carpetas obtenida correctamente.");
    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseDto<>(500, null, "Error obteniendo la lista de carpetas, " + e.getMessage());
    }
  }

  public ResponseDto<Boolean> createFolder(CreateFolderDto createFolderDto) {
    try {
      Long userId = createFolderDto.getIdUser();
      Optional<Long> parentFolderId = createFolderDto.getIdParentFolder();

      UserEntity user = usuarioRepository.findById(userId).get();

      FolderEntity folder = FolderMapper.createDtoToEntity(createFolderDto);
      folder.setUser(user);

      if (parentFolderId != null && parentFolderId.isPresent()) {
        FolderEntity parentFolder = folderRepository.findById(parentFolderId.get()).get();
        folder.setParentFolder(parentFolder);
      }

      folderRepository.save(folder);
      minioService.createBucketIfNotExists(folder.getCode());
      return new ResponseDto<>(201, true, "Carpeta creada correctamente");
    } catch (Exception e) {
      return new ResponseDto<>(500, false, "Error al crear la carpeta");
    }
  }

  @Transactional
  public ResponseDto<Boolean> deleteFolder(Long folderId) {
    try {
      List<Long> userIds = sharedFolderRepository.findUserIdsByFolderId(folderId);
      FolderEntity folder = folderRepository.findById(folderId).get();
      notificationService.sendDeleteFolderNotification(userIds, folder.getName());
      deleteChildFolders(folderId);
      folderRepository.deleteSharedFolderReferencesByFolderId(folderId);
      folderRepository.deleteDocumentsByFolderId(folderId);
      folderRepository.deleteById(folderId);
      folderRepository.clearParentFolderReferences(folderId);
      folderRepository.deleteById(folderId);
      return new ResponseDto<>(200, true, "Carpeta eliminada correctamente");
    } catch (Exception e) {
      return new ResponseDto<>(500, false, "Error al eliminar la carpeta");
    }
  }

  @Transactional
  private void deleteChildFolders(Long parentFolderId) throws Exception {
    List<FolderEntity> childFolders = folderRepository.findByParentFolder_Id(parentFolderId);
    for (FolderEntity childFolder : childFolders) {
      deleteChildFolders(childFolder.getId());
      minioService.deleteBucket(childFolder.getCode());
      folderRepository.deleteSharedFolderReferencesByFolderId(childFolder.getId());
      folderRepository.deleteDocumentsByFolderId(childFolder.getId());
      folderRepository.deleteById(childFolder.getId());
    }
  }

  public void shareFolder(Long folderId, Long emisorId, Long receptorId) {
    UserEntity emisor = usuarioRepository.findById(emisorId)
        .orElseThrow(() -> new RuntimeException("UserEntity emisor no encontrado con ID: " + emisorId));
    UserEntity receptor = usuarioRepository.findById(receptorId)
        .orElseThrow(() -> new RuntimeException("UserEntity receptor no encontrado con ID: " + receptorId));
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
        "Has recibido acceso a la carpeta '" + folder.getName() + "' de " + emisor.getNames() + " "
            + emisor.getFirstSurname(),
        "compartida");
  }

  //
  public String getDownloadUrl(String etag, String bucket) {
    try {
      String bucketName = bucket;

      Iterable<Result<Item>> myObjects = minioClient.listObjects(
          ListObjectsArgs.builder()
              .bucket(bucketName)
              .build());

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
    List<SharedFolder> receivedFolders = sharedFolderRepository.findByReceptor_id(userId);
    receivedFolders.forEach(
        sharedFolder -> uniqueFolders.put(sharedFolder.getFolder().getId(),
            FolderMapper.entityToDto(sharedFolder.getFolder())));

    // Procesar carpetas enviadas
    List<SharedFolder> sentFolders = sharedFolderRepository.findByEmisor_id(userId);
    sentFolders.forEach(
        sharedFolder -> uniqueFolders.put(sharedFolder.getFolder().getId(),
            FolderMapper.entityToDto(sharedFolder.getFolder())));

    // Devolver solo los valores del mapa, que son los FolderDto únicos
    return new ArrayList<>(uniqueFolders.values());
  }

  // Método para listar los contenidos de una carpeta compartida
  public FolderContentsDto listSharedFolderContents(Long userId, Long folderId) throws Exception {
    boolean isSharedWithUser = sharedFolderRepository.findByReceptor_idAndFolder_Id(userId, folderId).stream()
        .anyMatch(sf -> sf.getFolder().getId().equals(folderId));
    boolean isSharedByUser = sharedFolderRepository.findByEmisor_idAndFolder_Id(userId, folderId).stream()
        .anyMatch(sf -> sf.getFolder().getId().equals(folderId));

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
        // Aquí asumimos la existencia de un método que genera URLs de descarga; si no
        // existe, se debe implementar
        dto.setMinioLink("");
        return dto;
      }).collect(Collectors.toList());

      List<FolderDto> folderDtos = subFolders.stream().map(folder -> {
        FolderDto dto = new FolderDto();
        dto.setId(folder.getId());
        dto.setName(folder.getName());
        dto.setAccessType(folder.getAccessType());
        dto.setCreationDate(folder.getCreationDate());
        dto.setUpdateDate(folder.getUpdateDate());
        dto.setUserId(folder.getUser().getId());
        dto.setParentFolderId(folder.getParentFolder().getId());
        return dto;
      }).collect(Collectors.toList());
      return new FolderContentsDto(folderDtos, fileDtos);
    } else {
      throw new RuntimeException("La carpeta no ha sido compartida con este usuario o no existe.");
    }
  }

  // Método para listar los usuarios con acceso a una carpeta
  public List<UserDto> listUsersWithAccessToFolder(Long folderId) {
    List<SharedFolder> sharedFolders = sharedFolderRepository.findByFolder_Id(folderId);
    List<UserDto> usersWithAccess = new ArrayList<>();

    for (SharedFolder sharedFolder : sharedFolders) {
      UserEntity receptor = sharedFolder.getReceptor();
      UserDto dto = UserMapper.entityToDto(receptor);
      usersWithAccess.add(dto);
    }

    return usersWithAccess.stream().distinct().collect(Collectors.toList()); // Eliminar duplicados si fuera necesario
  }

  // comartir con toso los usaurios
  public void shareFolderWithAllUsers(Long folderId, Long emisorId) {
    List<UserEntity> usuarios = usuarioRepository.findAll();
    FolderEntity folder = folderRepository.findById(folderId)
        .orElseThrow(() -> new RuntimeException("Carpeta no encontrada con ID: " + folderId));
    for (UserEntity usuario : usuarios) {
      if (!usuario.getId().equals(emisorId)) {
        shareFolder(folderId, emisorId, usuario.getId());
        notificacionBl.crearNotificacionCompartir(usuario.getId(),
            "Nueva Carpeta Compartida",
            "Tienes acceso a una nueva carpeta: '" + folder.getName() + "' compartida por "
                + usuarioRepository.findById(emisorId).get().getNames(),
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
    List<UserEntity> usuariosEnDependencia = usuarioRepository.findByDependence(dependencyName);
    if (usuariosEnDependencia.isEmpty()) {
      throw new RuntimeException("No existen usuarios en la dependencia: " + dependencyName);
    }

    // Obtener el usuario emisor
    UserEntity emisor = usuarioRepository.findById(emisorId)
        .orElseThrow(() -> new RuntimeException("UserEntity emisor no encontrado con ID: " + emisorId));

    // Compartir la carpeta con cada usuario de la dependencia
    for (UserEntity receptor : usuariosEnDependencia) {
      if (!receptor.getId().equals(emisorId)) { // Asegurarse de no compartir la carpeta con uno mismo
        shareFolder(folderId, emisorId, receptor.getId());
        // Crear notificaciones para cada receptor que recibe acceso a la carpeta
        notificacionBl.crearNotificacionCompartir(receptor.getId(),
            "Carpeta Compartida",
            "Tienes acceso a una nueva carpeta: '" + folder.getName() + "' compartida por " + emisor.getNames(),
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
    folderDto.setUserId(folder.getUser().getId());
    folderDto.setParentFolderId(folder.getParentFolder() != null ? folder.getParentFolder().getId() : null);
    return folderDto;
  }

  // Método para obtener carpetas compartidas con un usuario específico
  public List<FolderDto> getSharedFoldersWithUser(Long userId) {
    List<SharedFolder> sharedFoldersByUser = sharedFolderRepository.findByReceptor_id(userId);
    Set<FolderDto> sharedFolders = new HashSet<>();

    sharedFoldersByUser.forEach(sharedFolder -> sharedFolders.add(FolderMapper.entityToDto(sharedFolder.getFolder())));

    return new ArrayList<>(sharedFolders);
  }

  // Método para descargar el contenido de un bucket
  public void downloadBucketContents(String bucketName, OutputStream outputStream) throws IOException {
    ZipOutputStream zipOut = new ZipOutputStream(outputStream);
    try {
      Iterable<Result<Item>> objects = minioClient.listObjects(
          ListObjectsArgs.builder().bucket(bucketName).build());

      for (Result<Item> objectResult : objects) {
        Item item = objectResult.get();
        InputStream stream = minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucketName)
                .object(item.objectName())
                .build());

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
