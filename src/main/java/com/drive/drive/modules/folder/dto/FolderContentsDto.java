package com.drive.drive.modules.folder.dto;

import com.drive.drive.modules.file.dto.FileDto;

import java.util.List;

public class FolderContentsDto {
  private List<FolderDto> folders;
  private List<FileDto> files;

  public FolderContentsDto(List<FolderDto> folders, List<FileDto> files) {
    this.folders = folders;
    this.files = files;
  }

  public List<FolderDto> getFolders() {
    return folders;
  }

  public void setFolders(List<FolderDto> folders) {
    this.folders = folders;
  }

  public List<FileDto> getFiles() {
    return files;
  }

  public void setFiles(List<FileDto> files) {
    this.files = files;
  }
}
