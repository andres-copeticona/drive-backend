package com.drive.drive.modules.qr.mappers;

import com.drive.drive.modules.file.mappers.FileMapper;
import com.drive.drive.modules.qr.dto.QrCodeDto;
import com.drive.drive.modules.user.entities.QrCodeEntity;
import com.drive.drive.modules.user.mappers.UserMapper;

public class QrCodeMapper {
  public static QrCodeDto entityToDto(QrCodeEntity entity) {
    QrCodeDto dto = new QrCodeDto();
    dto.setId(entity.getId());
    dto.setCode(entity.getCodeQr());
    dto.setTitle(entity.getTitle());
    dto.setMessage(entity.getMessage());
    dto.setFile(FileMapper.FileEntityToDto(entity.getFile()));
    dto.setEmitter(UserMapper.entityToDto(entity.getEmitter()));
    dto.setCreationDate(entity.getCreationDate());
    dto.setVisits(entity.getVisits());
    return dto;
  }
}
