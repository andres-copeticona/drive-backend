package com.drive.drive.modules.activities.dto;

import com.drive.drive.modules.user.dto.UserDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityDto {
  private String description;
  private String ip;
  private String activityType;
  private UserDto user;
}
