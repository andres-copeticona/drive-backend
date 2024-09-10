package com.drive.drive.modules.file.dto;

import org.springframework.data.jpa.domain.Specification;

import com.drive.drive.modules.file.entities.FileEntity;
import com.drive.drive.modules.file.entities.FileSpecification;
import com.drive.drive.shared.dto.BaseFilter;

import io.swagger.v3.oas.annotations.Parameter;
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

  @Override
  public Specification<FileEntity> getSpecification() {
    Specification<FileEntity> spec = Specification.where(null);

    if (createdBy != null && createdBy > 0)
      spec = spec.and(FileSpecification.filterByUser(createdBy));

    if (parentId != null && parentId > 0)
      spec = spec.and(FileSpecification.filterByFolder(parentId));

    if (searchTerm != null && !searchTerm.isBlank())
      spec = spec.and(FileSpecification.filterBySearchTerm(searchTerm));

    return spec;
  }

}
