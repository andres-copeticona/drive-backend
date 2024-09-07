package com.drive.drive.modules.folder.dto;

import lombok.Data;

@Data
public class ShareFolderRequest {
  private Long folderId;
  private Long emisorId;
  private Long receptorId;
}
