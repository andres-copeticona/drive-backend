package com.drive.drive.modules.user.services;

import com.drive.drive.modules.user.entities.QrCodeEntity;
import com.drive.drive.modules.user.repositories.QrCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.security.SecureRandom;

@Service
public class QrCodeService {

  @Autowired
  private QrCodeRepository qrCodeRepository;

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
  private final Random random = new SecureRandom();

  // Método para guardar un nuevo código QR
  public QrCodeEntity saveQrCode(QrCodeEntity qrCode) {
    qrCode.setCreationDate(new Date());
    qrCode = qrCodeRepository.save(qrCode);
    qrCode.setCodeQr(generateQrCodeString(qrCode));
    return qrCodeRepository.save(qrCode);
  }

  private String generateQrCodeString(QrCodeEntity qrCode) {
    String timeStamp = dateFormat.format(new Date());
    String uniqueCode = timeStamp + "_" + qrCode.getId() + "_" + random.nextInt(9999);
    return uniqueCode;
  }

  // Método para obtener un código QR por su ID
  public Optional<QrCodeEntity> getQrCodeById(Long id) {
    return qrCodeRepository.findById(id);
  }

  // Método para obtener un código QR por su código único
  public Optional<QrCodeEntity> getQrCodeByCodeQr(String codeQr) {
    return qrCodeRepository.findByCodeQr(codeQr);
  }
}
