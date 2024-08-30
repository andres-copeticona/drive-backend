package com.drive.drive.audit.dto;

public class ActividadCompartidoDto {
    private Long id;
    private double contador;
    private int idArchivo;
    private int idFolder;
    private int idUsuario;
    private String fecha;
    private String nombre;
    private String tipo;

    public ActividadCompartidoDto(Long id, double contador, int idArchivo, int idFolder, int idUsuario, String fecha, String nombre, String tipo) {
        this.id = id;
        this.contador = contador;
        this.idArchivo = idArchivo;
        this.idFolder = idFolder;
        this.idUsuario = idUsuario;
        this.fecha = fecha;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getContador() {
        return contador;
    }

    public void setContador(double contador) {
        this.contador = contador;
    }

    public int getIdArchivo() {
        return idArchivo;
    }

    public void setIdArchivo(int idArchivo) {
        this.idArchivo = idArchivo;
    }

    public int getIdFolder() {
        return idFolder;
    }

    public void setIdFolder(int idFolder) {
        this.idFolder = idFolder;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public ActividadCompartidoDto() {
    }
}
