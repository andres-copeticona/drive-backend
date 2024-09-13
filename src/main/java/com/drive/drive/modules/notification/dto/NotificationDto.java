package com.drive.drive.modules.notification.dto;

import java.time.LocalDateTime;

import com.drive.drive.modules.user.dto.UserDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
  UserDto user;
  String title;
  String message;
  String type;
  Boolean read;
  LocalDateTime date;
}
