package com.drive.drive.shared.dto;

import com.drive.drive.modules.file.dto.FileDto;

import lombok.Data;

@Data
public class QrDto {
  private FileDto file;
  private String qrCode;
}
