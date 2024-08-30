package com.drive.drive.audit.dto;

public class ContadorActividadDto {
    private String tipoActividad;
    private Long cantidad;

    public ContadorActividadDto(String tipoActividad, Long cantidad) {
        this.tipoActividad = tipoActividad;
        this.cantidad = cantidad;
    }

    public String getTipoActividad() {
        return tipoActividad;
    }

    public void setTipoActividad(String tipoActividad) {
        this.tipoActividad = tipoActividad;
    }

    public Long getCantidad() {
        return cantidad;
    }

    public void setCantidad(Long cantidad) {
        this.cantidad = cantidad;
    }
}
