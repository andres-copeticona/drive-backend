package com.drive.drive.modules.qr.dto;

import java.util.Date;

import com.drive.drive.modules.file.dto.FileDto;
import com.drive.drive.modules.user.dto.UserDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QrCodeDto {
  private Long id;
  private UserDto emitter;
  private String title;
  private String message;
  private Date creationDate;
  private FileDto file;
  private Integer visits;
}
