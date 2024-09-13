package com.drive.drive.shared.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.drive.drive.modules.file.entities.FileEntity;
import com.drive.drive.modules.file.entities.SharedFileEntity;
import com.drive.drive.modules.notification.dto.CreateNotificationDto;
import com.drive.drive.modules.notification.services.NotificationService;
import com.drive.drive.modules.user.entities.UserEntity;

@Service
public class SendNotificationService {
  private NotificationService notificationService;

  public SendNotificationService(NotificationService notificationSerice) {
    this.notificationService = notificationSerice;
  }

  public void sendDeleteFileNotification(List<SharedFileEntity> sharedDocuments, FileEntity file) {
    List<Long> userIds = sharedDocuments.stream()
        .map(doc -> doc.getReceptor().getId())
        .distinct()
        .collect(Collectors.toList());
    CreateNotificationDto dto = new CreateNotificationDto();
    dto.setTitle("Documento Eliminado");
    dto.setMessage("El documento '" + file.getTitle() + "' ha sido eliminado.");
    dto.setType("eliminado");
    for (Long userId : userIds) {
      dto.setUserId(userId);
      notificationService.createNotification(dto);
    }
  }

  public void sendShareFileNotification(List<Long> userIds, String fileName, String emisorName) {
    CreateNotificationDto dto = new CreateNotificationDto();
    dto.setTitle("Carpeta Compartida");
    dto.setMessage("Has recibido acceso al archivo '" + fileName + "', ha sido compartida por " + emisorName + ".");
    dto.setType("compartido");
    for (Long userId : userIds) {
      dto.setUserId(userId);
      notificationService.createNotification(dto);
    }
  }

  public void sendDeleteFolderNotification(List<Long> userIds, String folderName) {
    CreateNotificationDto dto = new CreateNotificationDto();
    dto.setTitle("Carpeta Eliminada");
    dto.setMessage("La carpeta '" + folderName + "' ha sido eliminada.");
    dto.setType("eliminado");
    for (Long userId : userIds) {
      dto.setUserId(userId);
      notificationService.createNotification(dto);
    }
  }

  public void sendShareFolderNotification(List<Long> userIds, String folderName, String emisorName) {
    CreateNotificationDto dto = new CreateNotificationDto();
    dto.setTitle("Carpeta Compartida");
    dto.setMessage("Has recibido acceso a la carpeta '" + folderName + "' ha sido compartida por " + emisorName + ".");
    dto.setType("compartido");
    for (Long userId : userIds) {
      dto.setUserId(userId);
      notificationService.createNotification(dto);
    }
  }

  public void sendAllShareFolderNotification(List<UserEntity> users, String folderName, String emisorName) {
    CreateNotificationDto dto = new CreateNotificationDto();
    dto.setTitle("Carpeta Compartida");
    dto.setMessage("Has recibido acceso a la carpeta '" + folderName + "', ha sido compartida por " + emisorName + ".");
    dto.setType("compartido");
    for (UserEntity user : users) {
      dto.setUserId(user.getId());
      notificationService.createNotification(dto);
    }
  }
}
