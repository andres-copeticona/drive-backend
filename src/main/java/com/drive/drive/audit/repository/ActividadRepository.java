package com.drive.drive.audit.repository;

import com.drive.drive.audit.entity.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {
  // Método para obtener actividades por usuario
  List<Actividad> findByUsuario_id(Long usuarioId);
}
