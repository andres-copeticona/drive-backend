package com.drive.drive.modules.file.dto;

import org.springframework.data.jpa.domain.Specification;

import com.drive.drive.modules.file.entities.SharedFileEntity;
import com.drive.drive.modules.file.entities.SharedFileSpecification;
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
public class SharedFileFilter extends BaseFilter<SharedFileEntity> {
  @Parameter(description = "User that share the file", example = "1", required = false)
  private Long emisorId;

  @Parameter(description = "User that receive the shared file", example = "1", required = true)
  private Long receptorId;

  @Override
  public Specification<SharedFileEntity> getSpecification() {
    Specification<SharedFileEntity> spec = Specification.where(null);

    if (emisorId != null && emisorId > 0)
      spec = spec.and(SharedFileSpecification.filterByEmisor(emisorId));

    if (receptorId != null && receptorId > 0)
      spec = spec.and(SharedFileSpecification.filterByReceptor(receptorId));

    if (searchTerm != null && !searchTerm.isBlank())
      spec = spec.and(SharedFileSpecification.filterBySearchTerm(searchTerm));

    return spec;
  }

}
