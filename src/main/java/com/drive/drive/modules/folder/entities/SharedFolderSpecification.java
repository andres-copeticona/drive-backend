package com.drive.drive.modules.folder.entities;

import org.springframework.data.jpa.domain.Specification;

public class SharedFolderSpecification {

  public static Specification<SharedFolderEntity> filterByEmisor(Long userId) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("emisor").get("id"), userId);
  }

  public static Specification<SharedFolderEntity> filterByReceptor(Long userId) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("receptor").get("id"), userId);
  }

  public static Specification<SharedFolderEntity> filterBySearchTerm(String searchTerm) {
    return (root, query, criteriaBuilder) -> {
      String searchPattern = "%" + searchTerm.toLowerCase() + "%";

      return criteriaBuilder.or(
          criteriaBuilder.like(criteriaBuilder.lower(root.get("folder").get("name")), searchPattern));
    };
  }
}
