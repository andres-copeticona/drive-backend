package com.drive.drive.modules.file.dto;

import org.springframework.data.jpa.domain.Specification;

import com.drive.drive.modules.file.entities.FileEntity;
import com.drive.drive.modules.file.entities.FileSpecification;
import com.drive.drive.shared.dto.BaseFilter;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileFilter extends BaseFilter<FileEntity> {
  @Parameter(description = "User that created the folder", example = "1", required = false)
  private Long createdBy;

  @Parameter(description = "Parent id", example = "1", required = false)
  private Long parentId;

  @Parameter(description = "Filter by category", required = false, schema = @Schema(allowableValues = { "Nuevo",
      "Sellado" }))
  private String category;

  @Parameter(description = "Filter by accessType", required = false, schema = @Schema(allowableValues = { "publico",
      "privado", "restringido" }))
  private String accessType;

  @Parameter(description = "Filter by file type (mime type)", required = false)
  private String type;

  @Override
  public Specification<FileEntity> getSpecification() {
    Specification<FileEntity> spec = Specification.where(null);

    if (createdBy != null && createdBy > 0)
      spec = spec.and(FileSpecification.filterByUser(createdBy));

    if (parentId != null && parentId > 0)
      spec = spec.and(FileSpecification.filterByFolder(parentId));

    if (searchTerm != null && !searchTerm.isBlank())
      spec = spec.and(FileSpecification.filterBySearchTerm(searchTerm));

    if (category != null && !category.isBlank())
      spec = spec.and(FileSpecification.filterByString("category", category));

    if (accessType != null && !accessType.isBlank())
      spec = spec.and(FileSpecification.filterByString("accessType", accessType));

    if (type != null && !type.isBlank())
      spec = spec.and(FileSpecification.filterByString("fileType", type));

    return spec;
  }

}
