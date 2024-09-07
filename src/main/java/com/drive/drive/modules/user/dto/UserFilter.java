package com.drive.drive.modules.user.dto;

import org.springframework.data.jpa.domain.Specification;

import com.drive.drive.modules.user.entities.UserEntity;
import com.drive.drive.modules.user.entities.UserSpecification;
import com.drive.drive.shared.dto.BaseFilter;

import lombok.ToString;

@ToString
public class UserFilter extends BaseFilter<UserEntity> {

  @Override
  public Specification<UserEntity> getSpecification() {
    Specification<UserEntity> spec = Specification.where(null);

    if (searchTerm != null && !searchTerm.isBlank())
      spec = spec.and(UserSpecification.filterBySearchTerm(searchTerm));

    return spec;
  }
}
