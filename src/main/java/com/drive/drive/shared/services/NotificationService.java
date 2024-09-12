package com.drive.drive.shared.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.drive.drive.audit.bl.NotificacionBl;
import com.drive.drive.modules.file.entities.FileEntity;
import com.drive.drive.sharing.entity.SharedDocumentEntity;

@Service
public class NotificationService {
  private NotificacionBl notificacionBl;

  public NotificationService(NotificacionBl notificacionBl) {
    this.notificacionBl = notificacionBl;
  }

  public void sendDeleteFileNotification(List<SharedDocumentEntity> sharedDocuments, FileEntity file) {
    List<Long> userIds = sharedDocuments.stream()
        .map(doc -> doc.getReceptor().getId())
        .distinct()
        .collect(Collectors.toList());

    String titulo = "Documento Eliminado";
    String mensaje = "El documento '" + file.getTitle() + "' ha sido eliminado.";
    for (Long userId : userIds) {
      notificacionBl.crearNotificacionCompartir(userId, titulo, mensaje, "eliminado");
    }
  }

  public void sendDeleteFolderNotification(List<Long> userIds, String folderName) {
    String titulo = "Carpeta Eliminada";
    String mensaje = "La carpeta '" + folderName + "' ha sido eliminada.";
    for (Long userId : userIds) {
      notificacionBl.crearNotificacionCompartir(userId, titulo, mensaje, "eliminado");
    }
  }

  public void sendShareFolderNotification(List<Long> userIds, String folderName, String emisorName) {
    String titulo = "Carpeta Compartida";
    String mensaje = "Has recibido acceso a la carpeta '" + folderName + "' ha sido compartida por " + emisorName + ".";
    for (Long userId : userIds) {
      notificacionBl.crearNotificacionCompartir(userId, titulo, mensaje, "compartido");
    }
  }
}
