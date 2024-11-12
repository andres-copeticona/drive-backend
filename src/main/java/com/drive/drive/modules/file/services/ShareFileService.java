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

import jakarta.servlet.http.HttpServletRequest;
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

  public ResponseDto<Boolean> share(CreateSharedFileDto shareFolderDto, HttpServletRequest request) {
    try {
      UserEntity emisor = userRepository.findById(shareFolderDto.getEmisorId()).get();
      FileEntity file = fileRepository.findById(shareFolderDto.getId()).get();

      String receptorNames = "";

      for (Long id : shareFolderDto.getReceptorIds()) {
        UserEntity user = userRepository.findById(id).get();
        SharedFileEntity sharedFolder = new SharedFileEntity();
        sharedFolder.setFile(file);
        sharedFolder.setEmisor(emisor);
        sharedFolder.setReceptor(user);
        sharedFolder.setType(shareFolderDto.getType());
        sharedFolder.setSharedAt(new Date());
        sharedFolder.setDeleted(false);
        sharedFileRepository.save(sharedFolder);
        file.setAccessType("compartido");
        fileRepository.save(file);
        receptorNames += user.getFullname() + ", ";
      }

      notificationService.sendShareFileNotification(
          shareFolderDto.getReceptorIds(), file.getTitle(), emisor.getFullname());

      request.setAttribute("log_description",
          "Archivo compartido " + file.getTitle() + " con " + receptorNames);

      return new ResponseDto<>(200, true, "Archivo compartido correctamente");
    } catch (Exception e) {
      return new ResponseDto<>(500, false, "Error al compartir el archivo");
    }
  }

  public ResponseDto<Boolean> shareAll(CreateSharedFileDto createSharedFileDto) {
    try {
      UserEntity emisor = userRepository.findById(createSharedFileDto.getEmisorId()).get();
      FileEntity folder = fileRepository.findById(createSharedFileDto.getId()).get();
      List<UserEntity> users = userRepository.findAll();

      for (UserEntity user : users) {
        SharedFileEntity sharedFolder = new SharedFileEntity();
        sharedFolder.setFile(folder);
        sharedFolder.setEmisor(emisor);
        sharedFolder.setReceptor(user);
        sharedFolder.setType(createSharedFileDto.getType());
        sharedFolder.setSharedAt(new Date());
        sharedFolder.setDeleted(false);
        sharedFileRepository.save(sharedFolder);
        folder.setAccessType("compartido");
        fileRepository.save(folder);
        notificationService.sendShareFileNotification(List.of(user.getId()), folder.getTitle(), emisor.getFullname());
      }

      return new ResponseDto<>(200, true, "Carpeta compartida correctamente");
    } catch (Exception e) {
      return new ResponseDto<>(500, false, "Error al compartir la carpeta");
    }
  }

  public ResponseDto<Boolean> shareDependency(CreateSharedFileDto createSharedFileDto) {
    try {
      UserEntity emisor = userRepository.findById(createSharedFileDto.getEmisorId()).get();
      FileEntity file = fileRepository.findById(createSharedFileDto.getId()).get();
      List<UserEntity> users = userRepository.findByDependence(createSharedFileDto.getDependency());

      for (UserEntity user : users) {
        SharedFileEntity sharedFolder = new SharedFileEntity();
        sharedFolder.setFile(file);
        sharedFolder.setEmisor(emisor);
        sharedFolder.setReceptor(user);
        sharedFolder.setType(createSharedFileDto.getType());
        sharedFolder.setSharedAt(new Date());
        sharedFolder.setDeleted(false);
        sharedFileRepository.save(sharedFolder);
        file.setAccessType("compartido");
        fileRepository.save(file);
        notificationService.sendShareFolderNotification(List.of(user.getId()), file.getTitle(), emisor.getFullname());
      }

      return new ResponseDto<>(200, true, "Archivo compartido correctamente");
    } catch (Exception e) {
      return new ResponseDto<>(500, false, "Error al compartir el archivo");
    }
  }
}
