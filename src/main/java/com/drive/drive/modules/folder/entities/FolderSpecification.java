package com.drive.drive.modules.folder.entities;

import org.springframework.data.jpa.domain.Specification;

public class FolderSpecification {

  public static Specification<FolderEntity> filterByUser(Long userId) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId);
  }

  public static Specification<FolderEntity> filterBySearchTerm(String searchTerm) {
    return (root, query, criteriaBuilder) -> {
      String searchPattern = "%" + searchTerm.toLowerCase() + "%";

      return criteriaBuilder.or(
          criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern));
    };
  }

  public static Specification<FolderEntity> filterByFolder(Long id) {
    return (root, query, criteriaBuilder) -> {
      if (id == 0) {
        return criteriaBuilder.isNull(root.get("parentFolder"));
      } else {
        return criteriaBuilder.equal(root.get("parentFolder").get("id"), id);
      }
    };
  }
}
