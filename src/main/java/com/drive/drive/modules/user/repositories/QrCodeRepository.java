package com.drive.drive.modules.user.repositories;

import com.drive.drive.modules.user.entities.QrCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QrCodeRepository extends JpaRepository<QrCodeEntity, Long> {
  Optional<QrCodeEntity> findByCodeQr(String codeQr);
}
