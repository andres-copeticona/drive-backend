package com.drive.drive.modules.file.mappers;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.drive.drive.modules.file.dto.CreateFilesDto;
import com.drive.drive.modules.file.dto.FileDto;
import com.drive.drive.modules.file.dto.UsageStorageDto;
import com.drive.drive.modules.file.entities.FileEntity;
import com.drive.drive.modules.folder.entities.FolderEntity;
import com.drive.drive.shared.utils.PasswordUtil;

public class FileMapper {

  public static FileEntity createFilesDtoToFileEntity(CreateFilesDto createFilesDto, MultipartFile file,
      FolderEntity folder) {
    String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
    String code = UUID.randomUUID().toString() + extension;

    FileEntity newFile = new FileEntity();
    newFile.setTitle(file.getOriginalFilename());
    newFile.setAccessType(createFilesDto.getAccessType());
    if (createFilesDto.getPassword() != null)
      newFile.setPassword(PasswordUtil.hashPassword(createFilesDto.getPassword()));
    newFile.setFolder(folder);
    newFile.setVisits(0);
    newFile.setMinioLink(folder.getCode() + "/" + code);
    newFile.setCode(code);
    newFile.setEtag("etag");
    newFile.setDeleted(false);
    newFile.setCategory("Nuevo");
    newFile.setSize(file.getSize());
    newFile.setFileType(file.getContentType());
    return newFile;
  }

  public static FileDto FileEntityToDto(FileEntity file) {
    FileDto fileDto = new FileDto();
    fileDto.setId(file.getId());
    fileDto.setTitle(file.getTitle());
    fileDto.setDescription(file.getDescription());
    fileDto.setVisits(file.getVisits());
    fileDto.setAccessType(file.getAccessType());
    fileDto.setPassword(file.getPassword());
    fileDto.setCreatedDate(file.getCreatedDate());
    fileDto.setModifiedDate(file.getModifiedDate());
    fileDto.setEtag(file.getEtag());
    fileDto.setFolderId(file.getFolder().getId());
    fileDto.setMinioLink("http://localhost:8080/api/v1/files/" + file.getCode() + "/view");
    fileDto.setCategory(file.getCategory());
    fileDto.setCode(file.getCode());
    fileDto.setSize(file.getSize());
    var qr = file.getQrCode();
    if (qr != null)
      fileDto.setQrId(qr.getId());
    fileDto.setFileType(file.getFileType());
    fileDto.setDeleted(file.getDeleted());
    fileDto.setUserId(file.getUser().getId());

    return fileDto;
  }

  public static UsageStorageDto FileEntityToUsageStorageDto(List<FileEntity> files) {
    UsageStorageDto usageStorageDto = new UsageStorageDto();
    Long totalUsage = 0L;
    Long documents = 0L;
    Long images = 0L;
    Long videos = 0L;
    Long audios = 0L;

    for (FileEntity file : files) {
      totalUsage += file.getSize();
      String mimeType = file.getFileType();
      if (mimeType != null) {
        if (mimeType.startsWith("image/")) {
          images += file.getSize();
        } else if (mimeType.startsWith("video/")) {
          videos += file.getSize();
        } else if (mimeType.startsWith("audio/")) {
          audios += file.getSize();
        } else if (mimeType.startsWith("application/")) {
          documents += file.getSize();
        }
      }
    }

    usageStorageDto.setTotalUsage(totalUsage);
    usageStorageDto.setDocuments(documents);
    usageStorageDto.setImages(images);
    usageStorageDto.setVideos(videos);
    usageStorageDto.setAudios(audios);

    return usageStorageDto;
  }
}
