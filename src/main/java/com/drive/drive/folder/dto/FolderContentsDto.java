package com.drive.drive.folder.dto;

import com.drive.drive.file.dto.FileDto;

import java.util.List;

public class FolderContentsDto {
    private List<FolderDto> folders;
    private List<FileDto> files;

    // Constructor que acepta dos listas
    public FolderContentsDto(List<FolderDto> folders, List<FileDto> files) {
        this.folders = folders;
        this.files = files;
    }

    // Getters y setters
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