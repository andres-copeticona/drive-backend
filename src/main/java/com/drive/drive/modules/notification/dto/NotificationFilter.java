package com.drive.drive.modules.notification.dto;

import org.springframework.data.jpa.domain.Specification;

import com.drive.drive.modules.notification.entities.NotificationEntity;
import com.drive.drive.modules.notification.entities.NotificationSpecification;
import com.drive.drive.shared.dto.BaseFilter;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NotificationFilter extends BaseFilter<NotificationEntity> {
  @Parameter(description = "User id", example = "1", required = false)
  private Long userId;

  @Parameter(description = "is the notification read", example = "true", required = true)
  private Boolean isRead = false;

  public Specification<NotificationEntity> getSpecification() {
    Specification<NotificationEntity> spec = Specification.where(null);

    if (userId != null && userId > 0)
      spec = spec.and(NotificationSpecification.filterByUser(userId));

    if (searchTerm != null && !searchTerm.isBlank())
      spec = spec.and(NotificationSpecification.filterBySearchTerm(searchTerm));

    if (isRead != null)
      spec = spec.and(NotificationSpecification.filterByIsRead(isRead));

    return spec;
  }
}
