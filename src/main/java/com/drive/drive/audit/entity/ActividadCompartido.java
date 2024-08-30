package com.drive.drive.audit.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table (name = "actividad_compartido")
public class ActividadCompartido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contador")
    private double contador;

    @Column(name = "id_archivo")
    private int idArchivo;

    @Column(name = "id_folder")
    private int idFolder;

    @Column(name = "id_usuario")
    private int idUsuario;

    @Column(name = "fecha")
    private String fecha;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "tipo")
    private String tipo;

    public ActividadCompartido() {
    }

    public ActividadCompartido(Long id, int contador, int idArchivo, int idFolder, int idUsuario, String nombre, String tipo) {
        this.id = id;
        this.contador = contador;
        this.idArchivo = idArchivo;
        this.idFolder = idFolder;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    @PrePersist
    public void prePersist() {
        this.fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
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
}
