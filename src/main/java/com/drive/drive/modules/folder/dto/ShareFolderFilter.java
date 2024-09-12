package com.drive.drive.modules.folder.dto;

import org.springframework.data.jpa.domain.Specification;

import com.drive.drive.modules.folder.entities.SharedFolderEntity;
import com.drive.drive.modules.folder.entities.SharedFolderSpecification;
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
public class ShareFolderFilter extends BaseFilter<SharedFolderEntity> {
  @Parameter(description = "User that share the folder", example = "1", required = false)
  private Long emisorId;

  @Parameter(description = "User that receive the shared folder", example = "1", required = true)
  private Long receptorId;

  @Override
  public Specification<SharedFolderEntity> getSpecification() {
    Specification<SharedFolderEntity> spec = Specification.where(null);

    if (emisorId != null && emisorId > 0)
      spec = spec.and(SharedFolderSpecification.filterByEmisor(emisorId));

    if (receptorId != null && receptorId > 0)
      spec = spec.and(SharedFolderSpecification.filterByReceptor(receptorId));

    if (searchTerm != null && !searchTerm.isBlank())
      spec = spec.and(SharedFolderSpecification.filterBySearchTerm(searchTerm));

    return spec;
  }

}
