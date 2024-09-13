package com.drive.drive.modules.notification.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.drive.drive.modules.notification.entities.NotificationEntity;

@Repository
public interface NotificationRepository
    extends JpaRepository<NotificationEntity, Long>, JpaSpecificationExecutor<NotificationEntity> {
  // // Método para obtener todas las notificaciones de un usuario específico
  // List<NotificationEntity> findByUsuarioId(Long usuarioId);
  //
  // // Método opcional para obtener notificaciones no leídas de un usuario
  // List<NotificationEntity> findByUsuarioIdAndLeidoFalse(Long usuarioId);
  //
  // // Método opcional para obtener notificaciones leídas de un usuario
  // List<NotificationEntity> findByUsuarioIdAndLeidoTrue(Long usuarioId);
}
