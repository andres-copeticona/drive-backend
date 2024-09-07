package com.drive.drive.audit.bl;

import com.drive.drive.audit.dto.ActividadDto;
import com.drive.drive.audit.dto.ContadorActividadDto;
import com.drive.drive.audit.entity.Actividad;
import com.drive.drive.audit.repository.ActividadRepository;
import com.drive.drive.modules.user.entities.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActividadBl {

  private final ActividadRepository actividadRepository;

  @Autowired
  public ActividadBl(ActividadRepository actividadRepository) {
    this.actividadRepository = actividadRepository;
  }

  // Método para crear una actividad
  @Transactional
  public ActividadDto crearActividad(ActividadDto actividadDto) {
    if (actividadDto.getUsuarioId() == null) {
      throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
    }

    Actividad actividad = new Actividad();
    actividad.setNombre(actividadDto.getNombre());
    actividad.setFecha(actividadDto.getFecha());
    actividad.setIp(actividadDto.getIp());

    // Crear un usuario con el ID proporcionado
    UserEntity usuario = new UserEntity();
    usuario.setId(actividadDto.getUsuarioId());

    actividad.setUsuario(usuario);
    actividad.setTipoActividad(actividadDto.getTipoActividad());

    Actividad nuevaActividad = actividadRepository.save(actividad);

    return convertirAActividadDto(nuevaActividad);
  }

  // Método para obtener todas las actividades
  @Transactional
  public List<ActividadDto> obtenerTodasLasActividades() {
    return actividadRepository.findAll().stream()
        .map(this::convertirAActividadDto)
        .collect(Collectors.toList());
  }

  // Método para obtener actividades por usuario
  @Transactional
  public List<ContadorActividadDto> obtenerContadorActividadesPorUsuario(Long usuarioId) {
    List<Actividad> actividades = actividadRepository.findByUsuario_id(usuarioId);

    Map<String, Long> contadorporTipo = actividades.stream()
        .collect(Collectors.groupingBy(Actividad::getTipoActividad, Collectors.counting()));

    return contadorporTipo.entrySet().stream()
        .map(entry -> new ContadorActividadDto(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  // Método para convertir Actividad a ActividadDto
  private ActividadDto convertirAActividadDto(Actividad actividad) {
    return new ActividadDto(
        actividad.getId(),
        actividad.getNombre(),
        actividad.getFecha(),
        actividad.getIp(),
        actividad.getUsuario().getId(), // Cambiado a ID de usuario
        actividad.getTipoActividad());
  }
}
