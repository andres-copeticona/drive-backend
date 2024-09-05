package com.drive.drive.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsuarioDTO {
  // Asumiendo que tienes JPA y Lombok, los omito para simplificar
  private Long usuarioID;
  private String idServidor;
  private String usuario;
  private String ci;
  private String nombres;
  private String paterno;
  private String materno;
  private String celular;
  private String domicilio;
  private String cargo;
  private String dependencia;
  private String sigla;
  private String fullName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private boolean deleted;
  private boolean status;
  private Set<RolDto> roles;

  @JsonSetter("status")
  public void setStatusFromString(String status) {
    this.status = "success".equals(status);
  }

}
