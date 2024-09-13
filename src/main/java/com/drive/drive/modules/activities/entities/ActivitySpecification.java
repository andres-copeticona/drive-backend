package com.drive.drive.modules.activities.entities;

import org.springframework.data.jpa.domain.Specification;

public class ActivitySpecification {

  public static Specification<ActivityEntity> filterByUser(Long userId) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId);
  }

  public static Specification<ActivityEntity> filterBySearchTerm(String searchTerm) {
    return (root, query, criteriaBuilder) -> {
      String searchPattern = "%" + searchTerm.toLowerCase() + "%";

      return criteriaBuilder.or(
          criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern),
          criteriaBuilder.like(criteriaBuilder.lower(root.get("ip")), searchPattern),
          criteriaBuilder.like(criteriaBuilder.lower(root.get("activityType")), searchPattern));
    };
  }
}
