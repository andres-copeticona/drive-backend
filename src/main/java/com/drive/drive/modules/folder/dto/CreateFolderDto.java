package com.drive.drive.modules.folder.dto;

import java.util.Optional;

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
@Schema(description = "DTO for creating a folder.")
public class CreateFolderDto {

  @Schema(description = "Name of the folder to create.")
  @NotBlank(message = "El nombre de la carpeta es requerido.")
  @Size(min = 6, max = 30, message = "La longitud del nombre de la carpeta debe estar entre 6 y 30 caracteres.")
  String folderName;

  @Schema(description = "ID of the parent folder, if any.", requiredMode = RequiredMode.NOT_REQUIRED)
  Optional<Long> idParentFolder;

  @Schema(description = "ID of the user.", requiredMode = RequiredMode.NOT_REQUIRED)
  @Null
  Long idUser;
}
