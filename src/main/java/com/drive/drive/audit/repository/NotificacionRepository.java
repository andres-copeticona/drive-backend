package com.drive.drive.audit.repository;
import com.drive.drive.audit.entity.Notificaciones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificaciones, Long> {
    // Método para obtener todas las notificaciones de un usuario específico
    List<Notificaciones> findByUsuarioId(Long usuarioId);

    // Método opcional para obtener notificaciones no leídas de un usuario
    List<Notificaciones> findByUsuarioIdAndLeidoFalse(Long usuarioId);

    // Método opcional para obtener notificaciones leídas de un usuario
    List<Notificaciones> findByUsuarioIdAndLeidoTrue(Long usuarioId);
}