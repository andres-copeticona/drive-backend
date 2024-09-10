package com.drive.drive.modules.file.entities;

import org.springframework.data.jpa.domain.Specification;

public class FileSpecification {

  public static Specification<FileEntity> filterByUser(Long userId) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId);
  }

  public static Specification<FileEntity> filterBySearchTerm(String searchTerm) {
    return (root, query, criteriaBuilder) -> {
      String searchPattern = "%" + searchTerm.toLowerCase() + "%";

      return criteriaBuilder.or(
          criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern));
    };
  }

  public static Specification<FileEntity> filterByFolder(Long id) {
    return (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("folder").get("id"), id);
    };
  }
}
