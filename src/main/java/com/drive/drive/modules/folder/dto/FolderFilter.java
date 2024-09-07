package com.drive.drive.modules.folder.dto;

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
  @Parameter(description = "User that created the folder", example = "1", required = false)
  private Long createdBy;

  @Override
  public Specification<FolderEntity> getSpecification() {
    Specification<FolderEntity> spec = Specification.where(null);

    if (createdBy != null && createdBy > 0)
      spec = spec.and(FolderSpecification.filterByUser(createdBy));

    if (searchTerm != null && !searchTerm.isBlank())
      spec = spec.and(FolderSpecification.filterBySearchTerm(searchTerm));

    return spec;
  }

}
