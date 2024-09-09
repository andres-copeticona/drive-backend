package com.drive.drive.modules.folder.mappers;

import java.util.UUID;
import java.util.Date;

import com.drive.drive.modules.folder.dto.CreateFolderDto;
import com.drive.drive.modules.folder.dto.FolderDto;
import com.drive.drive.modules.folder.entities.FolderEntity;

public class FolderMapper {
  public static FolderEntity createDtoToEntity(CreateFolderDto createFolderDto) {
    Date date = new Date();
    String code = UUID.randomUUID().toString();

    FolderEntity folderEntity = new FolderEntity();
    folderEntity.setName(createFolderDto.getName());
    folderEntity.setCreationDate(date);
    folderEntity.setAccessType("admin");
    folderEntity.setUpdateDate(date);
    folderEntity.setDeleted(false);
    folderEntity.setCode(code);
    return folderEntity;
  }

  public static FolderDto entityToDto(FolderEntity folderEntity) {
    FolderDto folderDto = new FolderDto();
    folderDto.setId(folderEntity.getId());
    folderDto.setName(folderEntity.getName());
    folderDto.setCode(folderEntity.getCode());
    folderDto.setAccessType(folderEntity.getAccessType());
    folderDto.setCreationDate(folderEntity.getCreationDate());
    folderDto.setUpdateDate(folderEntity.getUpdateDate());
    folderDto.setDeleted(folderEntity.getDeleted());
    folderDto.setUserId(folderEntity.getUser().getId());
    folderDto.setParentFolderId(folderEntity.getParentFolder() != null ? folderEntity.getParentFolder().getId() : null);

    return folderDto;
  }
}
