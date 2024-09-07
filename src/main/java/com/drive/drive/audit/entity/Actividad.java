package com.drive.drive.audit.entity;

import com.drive.drive.modules.user.entities.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "UsuarioID")
  private UserEntity usuario;

  @Column(name = "tipo_actividad")
  private String tipoActividad;

  @PrePersist
  protected void onCreate() {
    this.fecha = LocalDateTime.now();
  }
}
