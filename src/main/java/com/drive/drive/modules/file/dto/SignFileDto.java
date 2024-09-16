package com.drive.drive.modules.file.dto;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignFileDto {

  @Hidden
  private Long userId;

  private Long fileId;

  private MultipartFile file;

  private String title;

  private String description;

  private String qrCode;
}
