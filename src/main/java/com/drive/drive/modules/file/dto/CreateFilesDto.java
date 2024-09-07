package com.drive.drive.modules.file.dto;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
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

  @Schema(description = "ID of the user.", requiredMode = RequiredMode.NOT_REQUIRED)
  @Null
  private Long idUser;

  @Schema(description = "Code of the folder where the files will be uploaded.")
  @NotBlank(message = "El código de la carpeta es requerido")
  private String folderCode;

  @Schema(description = "Files to upload.")
  @Size(max = 6, message = "No se pueden subir más de 6 archivos a la vez.")
  private MultipartFile[] files;

  private String accessType;

  private String password;

  private Long folderId;
}
