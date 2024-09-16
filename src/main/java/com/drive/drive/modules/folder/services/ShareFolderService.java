package com.drive.drive.modules.folder.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.drive.drive.modules.folder.dto.FolderDto;
import com.drive.drive.modules.folder.dto.ResponseShareFolderDto;
import com.drive.drive.modules.folder.dto.ShareFolderDto;
import com.drive.drive.modules.folder.dto.ShareFolderFilter;
import com.drive.drive.modules.folder.entities.FolderEntity;
import com.drive.drive.modules.folder.entities.SharedFolderEntity;
import com.drive.drive.modules.folder.mappers.FolderMapper;
import com.drive.drive.modules.folder.mappers.SharedFolderMapper;
import com.drive.drive.modules.folder.repositories.FolderRepository;
import com.drive.drive.modules.folder.repositories.SharedFolderRepository;
import com.drive.drive.modules.user.entities.UserEntity;
import com.drive.drive.modules.user.repositories.UserRepository;
import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.ResponseDto;
import com.drive.drive.shared.services.SendNotificationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ShareFolderService {

  @Autowired
  private SharedFolderRepository sharedFolderRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private FolderRepository folderRepository;

  @Autowired
  private SendNotificationService notificationService;

  public ResponseDto<ListResponseDto<List<ResponseShareFolderDto>>> list(ShareFolderFilter filter) {
    try {
      Specification<SharedFolderEntity> spec = filter.getSpecification();
      Sort sort = filter.getSort();
      Pageable pageable = filter.getPageable();

      List<SharedFolderEntity> folders;
      Long total = 0L;

      if (pageable == null) {
        folders = sharedFolderRepository.findAll(spec, sort);
        total = Long.valueOf(folders.size());
      } else {
        var res = sharedFolderRepository.findAll(spec, pageable);
        folders = res.getContent();
        total = res.getTotalElements();
      }

      List<ResponseShareFolderDto> dtos = folders.stream().map(SharedFolderMapper::entityToDto)
          .collect(Collectors.toList());
      return new ResponseDto<>(200, new ListResponseDto<>(dtos, total), "Lista de carpetas obtenida correctamente.");
    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseDto<>(500, null, "Error obteniendo la lista de carpetas compartidas");
    }
  }

  public ResponseDto<Boolean> share(ShareFolderDto shareFolderDto) {
    try {
      UserEntity emisor = userRepository.findById(shareFolderDto.getEmisorId()).get();
      FolderEntity folder = folderRepository.findById(shareFolderDto.getId()).get();

      for (Long id : shareFolderDto.getReceptorIds()) {
        UserEntity user = userRepository.findById(id).get();
        SharedFolderEntity sharedFolder = new SharedFolderEntity();
        sharedFolder.setFolder(folder);
        sharedFolder.setEmisor(emisor);
        sharedFolder.setReceptor(user);
        sharedFolder.setType(shareFolderDto.getType());
        sharedFolder.setSharedAt(new Date());
        sharedFolderRepository.save(sharedFolder);
      }

      notificationService.sendShareFolderNotification(
          shareFolderDto.getReceptorIds(), folder.getName(), emisor.getFullname());

      return new ResponseDto<>(200, true, "Carpeta compartida correctamente");
    } catch (Exception e) {
      return new ResponseDto<>(500, false, "Error al compartir la carpeta");
    }
  }

  public ResponseDto<Boolean> shareAll(ShareFolderDto shareFolderDto) {
    try {
      UserEntity emisor = userRepository.findById(shareFolderDto.getEmisorId()).get();
      FolderEntity folder = folderRepository.findById(shareFolderDto.getId()).get();
      List<UserEntity> users = userRepository.findAll();

      for (UserEntity user : users) {
        SharedFolderEntity sharedFolder = new SharedFolderEntity();
        sharedFolder.setFolder(folder);
        sharedFolder.setEmisor(emisor);
        sharedFolder.setReceptor(user);
        sharedFolder.setType(shareFolderDto.getType());
        sharedFolder.setSharedAt(new Date());
        sharedFolderRepository.save(sharedFolder);
        notificationService.sendShareFolderNotification(List.of(user.getId()), folder.getName(), emisor.getFullname());
      }

      return new ResponseDto<>(200, true, "Carpeta compartida correctamente");
    } catch (Exception e) {
      return new ResponseDto<>(500, false, "Error al compartir la carpeta");
    }
  }

  public ResponseDto<Boolean> shareDependency(ShareFolderDto shareFolderDto) {
    try {
      UserEntity emisor = userRepository.findById(shareFolderDto.getEmisorId()).get();
      FolderEntity folder = folderRepository.findById(shareFolderDto.getId()).get();
      List<UserEntity> users = userRepository.findByDependence(shareFolderDto.getDependency());

      for (UserEntity user : users) {
        SharedFolderEntity sharedFolder = new SharedFolderEntity();
        sharedFolder.setFolder(folder);
        sharedFolder.setEmisor(emisor);
        sharedFolder.setReceptor(user);
        sharedFolder.setType(shareFolderDto.getType());
        sharedFolder.setSharedAt(new Date());
        sharedFolderRepository.save(sharedFolder);
        notificationService.sendShareFolderNotification(List.of(user.getId()), folder.getName(), emisor.getFullname());
      }

      return new ResponseDto<>(200, true, "Carpeta compartida correctamente");
    } catch (Exception e) {
      return new ResponseDto<>(500, false, "Error al compartir la carpeta");
    }
  }

  public ResponseDto<List<FolderDto>> getBreadcrumb(Long folderId, Long sharedId) {
    try {
      List<FolderEntity> breadcrumb = new ArrayList<>();
      Optional<FolderEntity> currentFolderOpt = folderRepository.findById(folderId);
      FolderEntity sharedFolderOpt = folderRepository.findById(sharedId).get();

      if (currentFolderOpt.isPresent()) {
        FolderEntity currentFolder = currentFolderOpt.get();
        breadcrumb.add(currentFolder);
        while (currentFolder.getParentFolder() != null
            && currentFolder.getParentFolder().getId() != sharedId) {
          currentFolder = currentFolder.getParentFolder();
          breadcrumb.add(currentFolder);
        }
        breadcrumb.add(sharedFolderOpt);
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
}
