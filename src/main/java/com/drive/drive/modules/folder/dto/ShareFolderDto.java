package com.drive.drive.modules.folder.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareFolderDto {
  private Long id;
  private Long emisorId;
  private String type;
  private String dependency;
  private List<Long> receptorIds;
}
