package com.drive.drive.modules.activities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateActivityDto {
  private String description;
  private String ip;
  private Long userId;
  private String activityType;
}
