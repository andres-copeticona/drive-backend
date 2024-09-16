package com.drive.drive.modules.file.dto;

import lombok.*;

import java.util.Date;

import com.drive.drive.modules.user.dto.UserDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SharedFileDto {
  private Long id;
  private UserDto emitter;
  private UserDto receptor;
  private FileDto file;
  private Date sharedAt;
  private String fileLink;
}
