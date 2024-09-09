package com.drive.drive.modules.user.entities;

import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
  public static Specification<UserEntity> filterBySearchTerm(String searchTerm) {
    return (root, query, criteriaBuilder) -> {
      String searchPattern = "%" + searchTerm.toLowerCase() + "%";

      return criteriaBuilder.or(
          criteriaBuilder.like(criteriaBuilder.lower(root.get("names")), searchPattern),
          criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), searchPattern),
          criteriaBuilder.like(criteriaBuilder.lower(root.get("firstSurname")), searchPattern),
          criteriaBuilder.like(criteriaBuilder.lower(root.get("secondSurname")), searchPattern));
    };
  }
}
