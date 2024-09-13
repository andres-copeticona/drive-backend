package com.drive.drive.modules.file.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateSharedFileDto {
  private List<Long> receptorIds;
  private Long emisorId;
  private String type;
  private Long id;
  // TODO:
}
