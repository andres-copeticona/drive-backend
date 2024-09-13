package com.drive.drive.modules.notification.entities;

import org.springframework.data.jpa.domain.Specification;

public class NotificationSpecification {

  public static Specification<NotificationEntity> filterByUser(Long userId) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId);
  }

  public static Specification<NotificationEntity> filterByIsRead(Boolean isRead) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("read"), isRead);
  }

  public static Specification<NotificationEntity> filterBySearchTerm(String searchTerm) {
    return (root, query, criteriaBuilder) -> {
      String searchPattern = "%" + searchTerm.toLowerCase() + "%";

      return criteriaBuilder.or(
          criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern),
          criteriaBuilder.like(criteriaBuilder.lower(root.get("message")), searchPattern),
          criteriaBuilder.like(criteriaBuilder.lower(root.get("type")), searchPattern));
    };
  }
}
