package com.drive.drive.modules.file.services;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.drive.drive.modules.file.dto.CreateSharedFileDto;
import com.drive.drive.modules.file.dto.SharedFileDto;
import com.drive.drive.modules.file.dto.SharedFileFilter;
import com.drive.drive.modules.file.entities.FileEntity;
import com.drive.drive.modules.file.entities.SharedFileEntity;
import com.drive.drive.modules.file.mappers.SharedFileMapper;
import com.drive.drive.modules.file.repositories.FileRepository;
import com.drive.drive.modules.file.repositories.SharedFileRepository;
import com.drive.drive.modules.user.entities.UserEntity;
import com.drive.drive.modules.user.repositories.UserRepository;
import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.ResponseDto;
import com.drive.drive.shared.services.SendNotificationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ShareFileService {

  @Autowired
  private SharedFileRepository sharedFileRepository;

  @Autowired
  private FileRepository fileRepository;

  @Autowired
  private SendNotificationService notificationService;

  @Autowired
  private UserRepository userRepository;

  public ResponseDto<ListResponseDto<List<SharedFileDto>>> list(SharedFileFilter filter) {
    try {
      Specification<SharedFileEntity> spec = filter.getSpecification();
      Sort sort = filter.getSort();
      Pageable pageable = filter.getPageable();

      List<SharedFileEntity> files;
      Long total = 0L;

      if (pageable == null) {
        files = sharedFileRepository.findAll(spec, sort);
        total = Long.valueOf(files.size());
      } else {
        var res = sharedFileRepository.findAll(spec, pageable);
        files = res.getContent();
        total = res.getTotalElements();
      }

      List<SharedFileDto> dtos = files.stream().map(SharedFileMapper::entityToDto)
          .collect(Collectors.toList());
      return new ResponseDto<>(200, new ListResponseDto<>(dtos, total), "Lista de archivos obtenida correctamente.");
    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseDto<>(500, null, "Error obteniendo la lista de archivos compartidas");
    }
  }

  public ResponseDto<Boolean> share(CreateSharedFileDto shareFolderDto) {
    try {
      UserEntity emisor = userRepository.findById(shareFolderDto.getEmisorId()).get();
      FileEntity file = fileRepository.findById(shareFolderDto.getId()).get();

      for (Long id : shareFolderDto.getReceptorIds()) {
        UserEntity user = userRepository.findById(id).get();
        SharedFileEntity sharedFolder = new SharedFileEntity();
        sharedFolder.setFile(file);
        sharedFolder.setEmisor(emisor);
        sharedFolder.setReceptor(user);
        sharedFolder.setType(shareFolderDto.getType());
        sharedFolder.setSharedAt(new Date());
        sharedFileRepository.save(sharedFolder);
      }

      notificationService.sendShareFileNotification(
          shareFolderDto.getReceptorIds(), file.getTitle(), emisor.getFullname());

      return new ResponseDto<>(200, true, "Archivo compartido correctamente");
    } catch (Exception e) {
      return new ResponseDto<>(500, false, "Error al compartir el archivo");
    }
  }
}