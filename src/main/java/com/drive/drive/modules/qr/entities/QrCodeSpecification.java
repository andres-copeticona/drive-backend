package com.drive.drive.modules.qr.entities;

import org.springframework.data.jpa.domain.Specification;

import com.drive.drive.modules.user.entities.QrCodeEntity;

public class QrCodeSpecification {

  public static Specification<QrCodeEntity> filterBySearchTerm(String searchTerm) {
    return (root, query, criteriaBuilder) -> {
      String searchPattern = "%" + searchTerm.toLowerCase() + "%";

      return criteriaBuilder.or(
          criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern));
    };
  }
}
