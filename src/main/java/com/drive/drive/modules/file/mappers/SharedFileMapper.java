package com.drive.drive.modules.file.mappers;

import com.drive.drive.modules.file.dto.SharedFileDto;
import com.drive.drive.modules.file.entities.SharedFileEntity;
import com.drive.drive.modules.user.mappers.UserMapper;

public class SharedFileMapper {
  public static SharedFileDto entityToDto(SharedFileEntity entity) {
    SharedFileDto dto = new SharedFileDto();
    dto.setId(entity.getId());
    dto.setReceptor(UserMapper.entityToDto(entity.getReceptor()));
    dto.setEmitter(UserMapper.entityToDto(entity.getEmisor()));
    dto.setFile(FileMapper.FileEntityToDto(entity.getFile()));
    dto.setSharedAt(entity.getSharedAt());
    dto.setFileLink(entity.getFileLink());
    return dto;
  }
}
