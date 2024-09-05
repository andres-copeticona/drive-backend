package com.drive.drive.user.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "Usuario")
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long usuarioID;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "RolID", nullable = false)
  @JsonManagedReference
  private Rol rol;
  private String idServidor;
  private String nombres;
  private String paterno;
  private String materno;
  private String celular;
  private String domicilio;
  @Column(unique = true)
  private String ci;
  private String estado;
  private String cargo;
  private String dependencia;
  private String sigla;
  @Column(unique = true)
  private String usuario;
  private String password;
  @Column(nullable = false)
  private boolean status = true;
  @Column(nullable = false, updatable = false)
  private Date createdAt = new Date();
  @Column(nullable = true)
  private Date updatedAt;
  @Column(nullable = false)
  private boolean deleted = false;

  public String getFullname() {
    return this.nombres + " " + this.paterno + " " + this.materno;
  }
}
