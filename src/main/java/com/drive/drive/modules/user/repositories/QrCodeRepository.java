package com.drive.drive.modules.user.repositories;

import com.drive.drive.modules.user.entities.QrCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface QrCodeRepository extends JpaRepository<QrCodeEntity, Long>, JpaSpecificationExecutor<QrCodeEntity> {
  Optional<QrCodeEntity> findByCodeQr(String codeQr);
}
