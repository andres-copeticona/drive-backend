package com.drive.drive.modules.notification.services;

import com.drive.drive.modules.notification.dto.AllNotificationDto;
import com.drive.drive.modules.notification.dto.CreateNotificationDto;
import com.drive.drive.modules.notification.dto.NotificationDto;
import com.drive.drive.modules.notification.dto.NotificationFilter;
import com.drive.drive.modules.notification.entities.NotificationEntity;
import com.drive.drive.modules.notification.mappers.NotificationMapper;
import com.drive.drive.modules.notification.repositories.NotificationRepository;
import com.drive.drive.modules.user.entities.UserEntity;
import com.drive.drive.modules.user.repositories.UserRepository;
import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.ResponseDto;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationService {

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private UserRepository userRepository;

  public ResponseDto<ListResponseDto<List<NotificationDto>>> listActivities(NotificationFilter filter) {
    try {
      Specification<NotificationEntity> spec = filter.getSpecification();
      Sort sort = filter.getSort();
      Pageable pageable = filter.getPageable();

      List<NotificationEntity> notifications;
      Long total = 0L;

      if (pageable == null) {
        notifications = notificationRepository.findAll(spec, sort);
        total = Long.valueOf(notifications.size());
      } else {
        var res = notificationRepository.findAll(spec, pageable);
        notifications = res.getContent();
        total = res.getTotalElements();
      }

      List<NotificationDto> dtos = notifications.stream().map(NotificationMapper::entityToDto)
          .collect(Collectors.toList());
      return new ResponseDto<>(200, new ListResponseDto<>(dtos, total),
          "Lista de notificaciones obtenida correctamente.");
    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseDto<>(500, null, "Error obteniendo la lista de notificationes");
    }
  }

  public ResponseDto<NotificationDto> createNotification(CreateNotificationDto createNotificationEntity) {
    try {
      NotificationEntity notification = NotificationMapper.createDtoToEntity(createNotificationEntity);

      UserEntity user = userRepository.findById(createNotificationEntity.getUserId()).get();
      notification.setUser(user);

      NotificationEntity res = notificationRepository.save(notification);
      return new ResponseDto<>(200, NotificationMapper.entityToDto(res), "Notificación creada correctamente");
    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseDto<>(500, null, "Error creando la notificación");
    }
  }

  public ResponseDto<Boolean> notifyToAllUsers(AllNotificationDto allNotificationDto) {
    try {
      userRepository.findAll().forEach(user -> {
        NotificationEntity notification = NotificationMapper.allDtoToEntity(allNotificationDto);
        notification.setUser(user);
        notificationRepository.save(notification);
      });
      return new ResponseDto<>(200, true, "Usuarios notificados correctamente");
    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseDto<>(500, false, "Error al notificar a todos los usuarios");
    }
  }

  public ResponseDto<Boolean> checkNotification(Long notificationId) {
    try {
      NotificationEntity notification = notificationRepository.findById(notificationId).get();
      notification.setRead(true);
      notificationRepository.save(notification);
      return new ResponseDto<>(200, true, "Notificación marcada como leída");
    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseDto<>(500, false, "Error al marcar como leída la notificación");
    }
  }
}
