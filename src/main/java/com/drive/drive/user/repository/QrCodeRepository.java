package com.drive.drive.user.repository;

import com.drive.drive.user.entity.QrCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QrCodeRepository extends JpaRepository<QrCodeEntity, Long> {
    Optional<QrCodeEntity> findByCodeQr(String codeQr);
}
