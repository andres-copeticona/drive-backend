package com.drive.drive.modules.file.dto;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CheckPasswordDto {
  @NotBlank(message = "La contraseña es requerida")
  @NotNull(message = "La contraseña es requerida")
  private String password;

  @Hidden
  private Long fileId;
}
