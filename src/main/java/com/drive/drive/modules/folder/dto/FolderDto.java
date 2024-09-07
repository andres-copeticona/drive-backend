package com.drive.drive.modules.folder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FolderDto {
  private Long id;
  private String name;
  private String code;
  private String accessType;
  private Date creationDate;
  private Date updateDate;
  private Boolean deleted;
  private Long userId;
  private Long parentFolderId;
}
