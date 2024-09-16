package com.drive.drive.modules.qr.dto;

import org.springframework.data.jpa.domain.Specification;

import com.drive.drive.modules.qr.entities.QrCodeSpecification;
import com.drive.drive.modules.user.entities.QrCodeEntity;
import com.drive.drive.shared.dto.BaseFilter;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QrCodeFilter extends BaseFilter<QrCodeEntity> {
  @Override
  public Specification<QrCodeEntity> getSpecification() {
    Specification<QrCodeEntity> spec = Specification.where(null);

    if (searchTerm != null && !searchTerm.isBlank())
      spec = spec.and(QrCodeSpecification.filterBySearchTerm(searchTerm));

    return spec;
  }
}
