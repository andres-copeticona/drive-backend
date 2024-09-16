package com.drive.drive.modules.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllNotificationDto {
  @NotBlank(message = "El título no puede estar vacío")
  @NotNull(message = "El título no puede ser nulo")
  private String title;

  @NotBlank(message = "El mensaje no puede estar vacío")
  @NotNull(message = "El mensaje no puede ser nulo")
  private String message;
}
