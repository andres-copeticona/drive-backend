package com.drive.drive.modules.folder.services;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.drive.drive.modules.folder.dto.ResponseShareFolderDto;
import com.drive.drive.modules.folder.dto.ShareFolderDto;
import com.drive.drive.modules.folder.dto.ShareFolderFilter;
import com.drive.drive.modules.folder.entities.FolderEntity;
import com.drive.drive.modules.folder.entities.SharedFolderEntity;
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
}
