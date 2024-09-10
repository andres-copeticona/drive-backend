package com.drive.drive.modules.file.mappers;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.drive.drive.modules.file.dto.CreateFilesDto;
import com.drive.drive.modules.file.dto.FileDto;
import com.drive.drive.modules.file.entities.FileEntity;
import com.drive.drive.modules.folder.entities.FolderEntity;

public class FileMapper {

  public static FileEntity createFilesDtoToFileEntity(CreateFilesDto createFilesDto, MultipartFile file,
      FolderEntity folder) {
    String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
    String code = UUID.randomUUID().toString() + extension;

    FileEntity newFile = new FileEntity();
    newFile.setTitle(file.getOriginalFilename());
    newFile.setAccessType(createFilesDto.getAccessType());
    newFile.setPassword(createFilesDto.getPassword());
    newFile.setFolder(folder);
    newFile.setMinioLink(folder.getCode() + "/" + code);
    newFile.setCode(code);
    newFile.setEtag("etag");
    newFile.setDeleted(false);
    newFile.setCategoria("Nuevo");
    newFile.setSize(file.getSize());
    newFile.setFileType(file.getContentType());
    return newFile;
  }

  public static FileDto FileEntityToDto(FileEntity file) {
    FileDto fileDto = new FileDto();
    fileDto.setId(file.getId());
    fileDto.setTitle(file.getTitle());
    fileDto.setDescription(file.getDescription());
    fileDto.setAccessType(file.getAccessType());
    fileDto.setPassword(file.getPassword());
    fileDto.setCreatedDate(file.getCreatedDate());
    fileDto.setModifiedDate(file.getModifiedDate());
    fileDto.setEtag(file.getEtag());
    fileDto.setFolderId(file.getFolder().getId());
    fileDto.setMinioLink(file.getMinioLink());
    fileDto.setCategoria(file.getCategoria());
    fileDto.setSize(file.getSize());
    fileDto.setFileType(file.getFileType());
    fileDto.setDeleted(file.getDeleted());
    fileDto.setUserId(file.getUser().getId());

    return fileDto;
  }
}
