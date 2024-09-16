package com.drive.drive.modules.notification.mappers;

import java.time.LocalDateTime;

import com.drive.drive.modules.notification.dto.AllNotificationDto;
import com.drive.drive.modules.notification.dto.CreateNotificationDto;
import com.drive.drive.modules.notification.dto.NotificationDto;
import com.drive.drive.modules.notification.entities.NotificationEntity;
import com.drive.drive.modules.user.mappers.UserMapper;

public class NotificationMapper {
  public static NotificationEntity createDtoToEntity(CreateNotificationDto createNotificationDto) {
    NotificationEntity notification = new NotificationEntity();

    notification.setTitle(createNotificationDto.getTitle());
    notification.setMessage(createNotificationDto.getMessage());
    notification.setDate(LocalDateTime.now());
    notification.setType(createNotificationDto.getType());
    notification.setRead(false);
    return notification;
  }

  public static NotificationDto entityToDto(NotificationEntity notificationEntity) {
    NotificationDto notificationDto = new NotificationDto();
    notificationDto.setId(notificationEntity.getId());
    notificationDto.setUser(UserMapper.entityToDto(notificationEntity.getUser()));
    notificationDto.setTitle(notificationEntity.getTitle());
    notificationDto.setMessage(notificationEntity.getMessage());
    notificationDto.setDate(notificationEntity.getDate());
    notificationDto.setRead(notificationEntity.getRead());
    notificationDto.setType(notificationEntity.getType());
    return notificationDto;
  }

  public static NotificationEntity allDtoToEntity(AllNotificationDto all) {
    NotificationEntity notification = new NotificationEntity();
    notification.setTitle(all.getTitle());
    notification.setMessage(all.getMessage());
    notification.setDate(LocalDateTime.now());
    notification.setType("general");
    notification.setRead(false);
    return notification;
  }
}
