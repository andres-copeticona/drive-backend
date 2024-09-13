package com.drive.drive.modules.file.entities;

import org.springframework.data.jpa.domain.Specification;

public class SharedFileSpecification {

  public static Specification<SharedFileEntity> filterByEmisor(Long userId) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("emisor").get("id"), userId);
  }

  public static Specification<SharedFileEntity> filterByReceptor(Long userId) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("receptor").get("id"), userId);
  }

  public static Specification<SharedFileEntity> filterBySearchTerm(String searchTerm) {
    return (root, query, criteriaBuilder) -> {
      String searchPattern = "%" + searchTerm.toLowerCase() + "%";

      return criteriaBuilder.or(
          criteriaBuilder.like(criteriaBuilder.lower(root.get("file").get("title")), searchPattern));
    };
  }
}
