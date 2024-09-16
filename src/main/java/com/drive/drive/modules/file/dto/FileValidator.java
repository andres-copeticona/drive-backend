package com.drive.drive.modules.file.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FileValidator {
  public static Integer maxFileSize = 1024 * 1024 * 1024;

  public boolean isValidFile(MultipartFile file) {
    final String fileName = file.getOriginalFilename();
    final String contentType = file.getContentType();
    return fileName != null
        && contentType != null
        && fileName.matches(".*\\.(jpg|jpeg|pdf|mp4|mp3)$");

  }
}
