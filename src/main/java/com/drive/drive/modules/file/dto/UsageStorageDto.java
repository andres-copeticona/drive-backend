package com.drive.drive.modules.file.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsageStorageDto {
  private Long totalUsage;
  private Long documents;
  private Long images;
  private Long videos;
  private Long audios;
}
