package com.drive.drive.modules.file.dto;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for creating files.")
public class CreateFilesDto {

  @Hidden
  private Long idUser;

  @Schema(description = "Files to upload.", requiredMode = RequiredMode.REQUIRED)
  @Size(max = 6, message = "No se pueden subir más de 6 archivos a la vez.")
  private MultipartFile[] files;

  @Schema(description = "Access type for the files", requiredMode = RequiredMode.REQUIRED, allowableValues = {
      "publico", "privado", "restringido" })
  @Pattern(regexp = "publico|privado|restringido", message = "El tipo de acceso debe ser 'publico', 'privado' o 'restringido'.")
  private String accessType;

  @Schema(description = "Password for restricted access.", requiredMode = RequiredMode.NOT_REQUIRED)
  private String password;

  @Schema(description = "ID of the folder where the files will be uploaded.", requiredMode = RequiredMode.REQUIRED)
  @NotNull(message = "El ID de la carpeta no puede ser nulo.")
  private Long folderId;
}
