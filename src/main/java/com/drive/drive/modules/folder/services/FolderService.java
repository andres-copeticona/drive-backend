package com.drive.drive.modules.folder.services;

import com.drive.drive.modules.file.entities.FileEntity;
import com.drive.drive.modules.file.repositories.FileRepository;
import com.drive.drive.modules.folder.dto.CreateFolderDto;
import com.drive.drive.modules.folder.dto.FolderDto;
import com.drive.drive.modules.folder.dto.FolderFilter;
import com.drive.drive.modules.folder.entities.FolderEntity;
import com.drive.drive.modules.folder.mappers.FolderMapper;
import com.drive.drive.modules.folder.repositories.FolderRepository;
import com.drive.drive.modules.folder.repositories.SharedFolderRepository;
import com.drive.drive.shared.dto.DownloadDto;
import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.ResponseDto;
import com.drive.drive.shared.services.MinioService;
import com.drive.drive.shared.services.SendNotificationService;
import com.drive.drive.modules.user.entities.UserEntity;
import com.drive.drive.modules.user.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class FolderService {

  @Autowired
  private SendNotificationService notificationService;

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

  public ResponseDto<FolderDto> getPublicFolderByCode(String code) {
    try {
      FolderEntity folder = folderRepository.findByCodeAndAccessTypeAndDeletedFalse(code, "publico").get();
      folder.setVisits(folder.getVisits() + 1);
      folderRepository.save(folder);

      return new ResponseDto<>(200, FolderMapper.entityToDto(folder), "Carpeta obtenida correctamente.");
    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseDto<>(500, null, "Error obteniendo la carpeta, " + e.getMessage());
    }
  }

  public ResponseDto<List<FolderDto>> getBreadcrumb(Long folderId) {
    try {
      List<FolderEntity> breadcrumb = new ArrayList<>();
      Optional<FolderEntity> currentFolderOpt = folderRepository.findById(folderId);

      if (currentFolderOpt.isPresent()) {
        FolderEntity currentFolder = currentFolderOpt.get();
        breadcrumb.add(currentFolder);
        while (currentFolder.getParentFolder() != null) {
          currentFolder = currentFolder.getParentFolder();
          breadcrumb.add(currentFolder);
        }
      }

      List<FolderDto> dtos = breadcrumb.stream()
          .map(FolderMapper::entityToDto)
          .collect(Collectors.toList());
      List<FolderDto> reversedBreadcrumb = new ArrayList<>();
      for (int i = dtos.size() - 1; i >= 0; i--) {
        reversedBreadcrumb.add(dtos.get(i));
      }

      return new ResponseDto<>(200, reversedBreadcrumb, "Breadcrumb obtenido correctamente.");
    } catch (Exception e) {
      return new ResponseDto<>(500, null, "Error obteniendo el breadcrumb, " + e.getMessage());
    }
  }

  public DownloadDto download(Long folderId) {
    try {
      FolderEntity folder = folderRepository.findById(folderId).get();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ZipOutputStream zipOut = new ZipOutputStream(baos);

      DownloadDto downloadDto = new DownloadDto();
      downloadDto.setName(folder.getName() + ".zip");
      downloadFolderContents(folder, "", zipOut);
      zipOut.close();
      downloadDto.setData(baos.toByteArray());

      return downloadDto;
    } catch (Exception e) {
      log.error(e.getMessage());
      return null;
    }
  }

  private void downloadFolderContents(FolderEntity folder, String parentPath, ZipOutputStream zipOut) {
    try {
      List<FileEntity> files = fileRepository.findByFolder_Id(folder.getId());

      for (FileEntity file : files) {
        try (InputStream stream = minioService.download(folder.getCode(), file.getCode())) {
          String fileName = parentPath + file.getTitle();
          ZipEntry zipEntry = new ZipEntry(fileName);
          zipOut.putNextEntry(zipEntry);
          byte[] bytes = new byte[1024];
          int length;
          while ((length = stream.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
          }
          zipOut.closeEntry();
        }
      }

      List<FolderEntity> subFolders = folderRepository.findByParentFolder_Id(folder.getId());
      for (FolderEntity subFolder : subFolders) {
        downloadFolderContents(subFolder, parentPath + subFolder.getName() + "/", zipOut);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new RuntimeException("Error al descargar el contenido de la carpeta: " + e.getMessage(), e);
    }
  }

  public ResponseDto<Boolean> createFolder(CreateFolderDto createFolderDto) {
    try {
      Long userId = createFolderDto.getIdUser();
      Long parentFolderId = createFolderDto.getParentId();

      UserEntity user = usuarioRepository.findById(userId).get();

      FolderEntity folder = FolderMapper.createDtoToEntity(createFolderDto);
      folder.setUser(user);

      if (parentFolderId != null) {
        FolderEntity parentFolder = folderRepository.findById(parentFolderId).get();
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

  public ResponseDto<String> togglePrivacity(Long id) {
    try {
      FolderEntity folder = folderRepository.findById(id).get();
      String accessType = folder.getAccessType().equals("publico") ? "privado" : "publico";
      folder.setAccessType(accessType);
      folderRepository.save(folder);
      return new ResponseDto<>(200, accessType, "Privacidad de la carpeta cambiada correctamente");
    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseDto<>(500, null, "Error al cambiar la privacidad de la carpeta");
    }
  }
}
