package com.drive.drive.modules.activities.dto;

import org.springframework.data.jpa.domain.Specification;

import com.drive.drive.modules.activities.entities.ActivityEntity;
import com.drive.drive.modules.activities.entities.ActivitySpecification;
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
public class ActivityFilter extends BaseFilter<ActivityEntity> {
  @Parameter(description = "User id", example = "1", required = false)
  private Long userId;

  @Override
  public Specification<ActivityEntity> getSpecification() {
    Specification<ActivityEntity> spec = Specification.where(null);

    if (userId != null && userId > 0)
      spec = spec.and(ActivitySpecification.filterByUser(userId));

    if (searchTerm != null && !searchTerm.isBlank())
      spec = spec.and(ActivitySpecification.filterBySearchTerm(searchTerm));

    return spec;
  }
}
