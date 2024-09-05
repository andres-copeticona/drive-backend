package com.drive.drive.audit.bl;

import com.drive.drive.audit.dto.NotificacionDto;
import com.drive.drive.audit.entity.Notificaciones;
import com.drive.drive.audit.repository.NotificacionRepository;
import com.drive.drive.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionBl {

  @Autowired
  private NotificacionRepository notificacionRepository;

  @Autowired
  private UserRepository usuarioRepository;

  // Método para crear una notificación
  public void crearNotificacionCompartir(Long usuarioId, String titulo, String mensaje, String tipo) {
    Notificaciones notificacion = new Notificaciones();
    notificacion.setUsuarioId(usuarioId);
    notificacion.setTitulo(titulo);
    notificacion.setMensaje(mensaje);
    notificacion.setFecha(LocalDateTime.now());
    notificacion.setTipo(tipo);
    notificacion.setLeido(false);
    notificacionRepository.save(notificacion);
  }

  // Método para crear una notificación masiva
  public void crearNotificacionMasiva(String titulo, String mensaje, String tipo) {
    usuarioRepository.findAll().forEach(usuario -> {
      Notificaciones notificacion = new Notificaciones();
      notificacion.setUsuarioId(usuario.getUsuarioID());
      notificacion.setTitulo(titulo);
      notificacion.setMensaje(mensaje);
      notificacion.setFecha(LocalDateTime.now());
      notificacion.setTipo(tipo);
      notificacion.setLeido(false);
      notificacionRepository.save(notificacion);
    });
  }

  // Método para notificar un cambio importante a un usuario
  public void notificarCambioImportante(Long usuarioId, String titulo, String mensaje, String tipo) {
    crearNotificacionCompartir(usuarioId, titulo, mensaje, tipo);
  }

  // Método para obtener todas las notificaciones de un usuario
  public List<NotificacionDto> obtenerNotificacionesPorUsuario(Long usuarioId) {
    return notificacionRepository.findByUsuarioId(usuarioId).stream()
        .map(this::convertirANotificacionDto)
        .collect(Collectors.toList());
  }

  // Método opcional para obtener notificaciones no leídas de un usuario
  public void marcarNotificacionComoLeida(Long notificacionId) {
    Notificaciones notificacion = notificacionRepository.findById(notificacionId)
        .orElseThrow(() -> new RuntimeException("Notificación no encontrada con ID: " + notificacionId));

    notificacion.setLeido(true);
    notificacionRepository.save(notificacion);
  }

  // Método opcional para obtener notificaciones no leídas de un usuario
  public List<NotificacionDto> obtenerNotificacionesNoLeidasPorUsuario(Long usuarioId) {
    return notificacionRepository.findByUsuarioIdAndLeidoFalse(usuarioId).stream()
        .map(this::convertirANotificacionDto)
        .collect(Collectors.toList());
  }

  // Método opcional para obtener notificaciones leídas de un usuario
  public List<NotificacionDto> obtenerNotificacionesLeidasPorUsuario(Long usuarioId) {
    return notificacionRepository.findByUsuarioIdAndLeidoTrue(usuarioId).stream()
        .map(this::convertirANotificacionDto)
        .collect(Collectors.toList());
  }

  // Convertir entidad a DTO
  private NotificacionDto convertirANotificacionDto(Notificaciones notificacion) {
    NotificacionDto dto = new NotificacionDto();
    dto.setId(notificacion.getId());
    dto.setUsuarioId(notificacion.getUsuarioId());
    dto.setTitulo(notificacion.getTitulo());
    dto.setFecha(notificacion.getFecha());
    dto.setMensaje(notificacion.getMensaje());
    dto.setTipo(notificacion.getTipo());
    dto.setLeido(notificacion.getLeido());
    return dto;
  }
}
