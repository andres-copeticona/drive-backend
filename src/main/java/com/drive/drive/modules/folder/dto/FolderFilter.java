package com.drive.drive.modules.folder.dto;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;

import com.drive.drive.modules.folder.entities.FolderEntity;
import com.drive.drive.modules.folder.entities.FolderSpecification;
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
public class FolderFilter extends BaseFilter<FolderEntity> {
  @Parameter(description = "User that upload the file", example = "1", required = false)
  private Long createdBy;

  @Parameter(description = "Folder id", example = "1", required = true)
  @NotNull
  private Long parentId;

  @Parameter(description = "Access type", example = "public", required = false)
  private String accessType;

  @Override
  public Specification<FolderEntity> getSpecification() {
    Specification<FolderEntity> spec = Specification.where(null);

    if (createdBy != null && createdBy > 0)
      spec = spec.and(FolderSpecification.filterByUser(createdBy));

    if (parentId != null && parentId > 0)
      spec = spec.and(FolderSpecification.filterByFolder(parentId));
    else
      spec = spec.and(FolderSpecification.filterByFolder(0L));

    if (searchTerm != null && !searchTerm.isBlank())
      spec = spec.and(FolderSpecification.filterBySearchTerm(searchTerm));

    if (accessType != null && !accessType.isBlank())
      spec = spec.and(FolderSpecification.filterByString("accessType", accessType));

    return spec;
  }
}
