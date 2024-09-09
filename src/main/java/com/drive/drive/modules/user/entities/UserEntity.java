package com.drive.drive.modules.user.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(name = "Usuario")
public class UserEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "UsuarioID")
  private Long id;

  @ManyToOne()
  @JoinColumn(name = "rolID", nullable = false)
  @JsonManagedReference
  private RoleEntity role;
  @Column(name = "idServidor")
  private String idServer;

  @Column(name = "nombres")
  private String names;

  @Column(name = "paterno")
  private String firstSurname;

  @Column(name = "materno")
  private String secondSurname;

  @Column(name = "celular")
  private String cellphone;

  @Column(name = "domicilio")
  private String address;

  @Column(unique = true)
  private String ci;

  @Column(name = "estado")
  private String state;

  @Column(name = "cargo")
  private String position;

  @Column(name = "dependencia")
  private String dependence;

  @Column(name = "sigla")
  private String acronym;

  @Column(name = "usuario", unique = true)
  private String username;

  @Column(name = "contrasenia")
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
    return this.names + " " + this.firstSurname + " " + this.secondSurname;
  }
}
