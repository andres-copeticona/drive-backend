package com.drive.drive.audit.entity;

import com.drive.drive.user.entity.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "actividades")
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @Column(name = "ip")
    private String ip;

    @ManyToOne(fetch = FetchType.LAZY) // Añadido para establecer la relación con Usuario
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "tipo_actividad")
    private String tipoActividad;

    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now();
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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTipoActividad() {
        return tipoActividad;
    }

    public void setTipoActividad(String tipoActividad) {
        this.tipoActividad = tipoActividad;
    }
}
