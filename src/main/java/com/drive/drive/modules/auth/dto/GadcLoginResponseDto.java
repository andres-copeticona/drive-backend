package com.drive.drive.modules.auth.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GadcLoginResponseDto {
  private Long id;
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
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private boolean deleted;
  private boolean status;

  @JsonSetter("status")
  public void setStatusFromString(String status) {
    this.status = "success".equals(status);
  }
}
