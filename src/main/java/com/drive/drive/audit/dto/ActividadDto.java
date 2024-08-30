package com.drive.drive.audit.dto;

import java.time.LocalDateTime;

public class ActividadDto {
    private Long id;
    private String nombre;
    private LocalDateTime fecha;
    private String ip;
    private Long usuarioId; // Cambiado a ID de usuario
    private String tipoActividad;

    // Constructores

    public ActividadDto() {
    }

    public ActividadDto(Long id, String nombre, LocalDateTime fecha, String ip, Long usuarioId, String tipoActividad) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.ip = ip;
        this.usuarioId = usuarioId;
        this.tipoActividad = tipoActividad;
    }

    // Getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTipoActividad() {
        return tipoActividad;
    }

    public void setTipoActividad(String tipoActividad) {
        this.tipoActividad = tipoActividad;
    }
}
