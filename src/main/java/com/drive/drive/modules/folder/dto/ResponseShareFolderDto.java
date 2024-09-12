package com.drive.drive.modules.folder.dto;

import java.sql.Date;

import com.drive.drive.modules.user.dto.UserDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseShareFolderDto {
  private Long id;
  private FolderDto folder;
  private UserDto emisor;
  private UserDto receptor;
  private Date sharedAt;
}
