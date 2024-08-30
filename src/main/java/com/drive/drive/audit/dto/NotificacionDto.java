package com.drive.drive.audit.dto;

import java.time.LocalDateTime;

public class NotificacionDto {

    private Long id;
    private Long usuarioId;
    private String titulo;
    private String mensaje;
    private LocalDateTime fecha;
    private Boolean leido;
    private String tipo;

    // Constructor vacío
    public NotificacionDto() {
    }

    // Constructor con parámetros
    public NotificacionDto(Long id, Long usuarioId, String titulo, String mensaje, LocalDateTime fecha, Boolean leido, String tipo) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.leido = leido;
        this.tipo = tipo;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Boolean getLeido() {
        return leido;
    }

    public void setLeido(Boolean leido) {
        this.leido = leido;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    // Override toString() method if needed for debugging
    @Override
    public String toString() {
        return "NotificacionDto{" +
                "id=" + id +
                ", usuarioId=" + usuarioId +
                ", titulo='" + titulo + '\'' +
                ", mensaje='" + mensaje + '\'' +
                ", fecha=" + fecha +
                ", leido=" + leido +
                ", tipo='" + tipo + '\'' +
                '}';
    }
}
