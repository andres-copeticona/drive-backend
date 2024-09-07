package com.drive.drive.modules.file.dto;

import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FileDto {

  private Long id;

  private String title;

  private String description;

  private String etag;

  private String accessType;

  private Date createdDate;

  private Date modifiedDate;

  private Boolean deleted;

  private Long userId;

  private Long folderId;

  private String password;

  private String minioLink;

  private String categoria;

  private Long size;

  private String fileType;
}
