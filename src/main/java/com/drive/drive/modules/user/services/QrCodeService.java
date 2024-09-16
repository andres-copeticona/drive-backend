package com.drive.drive.modules.user.services;

import com.drive.drive.modules.file.dto.FileDto;
import com.drive.drive.modules.file.entities.FileEntity;
import com.drive.drive.modules.file.mappers.FileMapper;
import com.drive.drive.modules.qr.dto.QrCodeDto;
import com.drive.drive.modules.user.entities.QrCodeEntity;
import com.drive.drive.modules.user.mappers.UserMapper;
import com.drive.drive.modules.user.repositories.QrCodeRepository;
import com.drive.drive.shared.dto.ResponseDto;
import com.drive.drive.shared.services.MinioService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.security.SecureRandom;

@Slf4j
@Service
public class QrCodeService {

  @Autowired
  private QrCodeRepository qrCodeRepository;

  @Autowired
  private MinioService minioService;

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

  public ResponseDto<QrCodeDto> getQrCodeByCodeQr(String codeQr) {
    try {
      QrCodeEntity qr = qrCodeRepository.findByCodeQr(codeQr).get();

      qr.setVisits(qr.getVisits() + 1);
      qrCodeRepository.save(qr);

      QrCodeDto qrDto = new QrCodeDto();
      qrDto.setId(qr.getId());
      qrDto.setCreationDate(qr.getCreationDate());
      qrDto.setTitle(qr.getTitle());
      qrDto.setMessage(qr.getMessage());
      qrDto.setEmitter(UserMapper.entityToDto(qr.getEmitter()));
      qrDto.setVisits(qr.getVisits());
      FileEntity file = qr.getFile();
      FileDto fileDto = FileMapper.FileEntityToDto(file);
      fileDto.setLink(minioService.getDownloadUrl(file.getFolder().getCode(), file.getCode()));
      qrDto.setFile(fileDto);

      return new ResponseDto<>(200, qrDto, "Código QR encontrado");
    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseDto<>(404, null, "Código QR no encontrado");
    }
  }
}
