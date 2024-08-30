package com.drive.drive.audit.dto;

public class ArchivoContadorDto {
    private int idArchivo;
    private int idFolder;
    private double contadorSumado;
    private String nombre;
    private String tipo;

    public ArchivoContadorDto(int idArchivo, int idFolder, double contadorSumado, String nombre, String tipo) {
        this.idArchivo = idArchivo;
        this.idFolder = idFolder;
        this.contadorSumado = contadorSumado;
        this.nombre = nombre;
        this.tipo = tipo;
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

    public double getContadorSumado() {
        return contadorSumado;
    }

    public void setContadorSumado(double contadorSumado) {
        this.contadorSumado = contadorSumado;
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
}
