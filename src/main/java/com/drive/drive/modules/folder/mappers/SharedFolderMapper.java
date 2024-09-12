package com.drive.drive.modules.folder.mappers;

import java.util.Date;

import com.drive.drive.modules.folder.dto.ResponseShareFolderDto;
import com.drive.drive.modules.folder.dto.ShareFolderDto;
import com.drive.drive.modules.folder.entities.SharedFolderEntity;
import com.drive.drive.modules.user.mappers.UserMapper;

public class SharedFolderMapper {
  public static SharedFolderEntity shareDtoToEntity(ShareFolderDto shareFolderDto) {
    SharedFolderEntity sharedFolderEntity = new SharedFolderEntity();
    sharedFolderEntity.setSharedAt(new Date());
    return sharedFolderEntity;
  }

  public static ResponseShareFolderDto entityToDto(SharedFolderEntity sharedFolderEntity) {
    ResponseShareFolderDto responseShareFolderDto = new ResponseShareFolderDto();
    responseShareFolderDto.setId(sharedFolderEntity.getId());
    responseShareFolderDto.setFolder(FolderMapper.entityToDto(sharedFolderEntity.getFolder()));
    responseShareFolderDto.setEmisor(UserMapper.entityToDto(sharedFolderEntity.getEmisor()));
    responseShareFolderDto.setReceptor(UserMapper.entityToDto(sharedFolderEntity.getReceptor()));
    return responseShareFolderDto;
  }
}
