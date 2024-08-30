package com.drive.drive.audit.api;


import com.drive.drive.audit.bl.NotificacionBl;
import com.drive.drive.audit.dto.NotificacionDto;
import com.drive.drive.audit.dto.NotificacionMasivaRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotifiacionController {

    @Autowired
    private NotificacionBl notificacionBl;

    // Obtener todas las notificaciones de un usuario
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<NotificacionDto>> obtenerNotificacionesPorUsuario(@PathVariable Long usuarioId) {
        try {
            List<NotificacionDto> notificaciones = notificacionBl.obtenerNotificacionesPorUsuario(usuarioId);
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Marcar una notificación como leída
    @PostMapping("/marcar-como-leida/{notificacionId}")
    public ResponseEntity<String> marcarNotificacionComoLeida(@PathVariable Long notificacionId) {
        try {
            notificacionBl.marcarNotificacionComoLeida(notificacionId);
            return ResponseEntity.ok("Notificación marcada como leída.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Obtener notificaciones no leídas de un usuario
    @GetMapping("/no-leidas/usuario/{usuarioId}")
    public ResponseEntity<List<NotificacionDto>> obtenerNotificacionesNoLeidasPorUsuario(@PathVariable Long usuarioId) {
        try {
            List<NotificacionDto> notificaciones = notificacionBl.obtenerNotificacionesNoLeidasPorUsuario(usuarioId);
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Obtener notificaciones leídas de un usuario
    @GetMapping("/leidas/usuario/{usuarioId}")
    public ResponseEntity<List<NotificacionDto>> obtenerNotificacionesLeidasPorUsuario(@PathVariable Long usuarioId) {
        try {
            List<NotificacionDto> notificaciones = notificacionBl.obtenerNotificacionesLeidasPorUsuario(usuarioId);
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Endpoint para crear notificaciones masivas
    @PostMapping("/crear-masiva")
    public ResponseEntity<String> crearNotificacionMasiva(@RequestBody NotificacionMasivaRequest request) {
        try {
            notificacionBl.crearNotificacionMasiva(request.getTitulo(), request.getMensaje(), request.getTipo());
            return ResponseEntity.ok("Notificaciones masivas creadas correctamente.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear notificaciones masivas: " + e.getMessage());
        }
    }
}
