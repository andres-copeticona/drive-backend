package com.drive.drive.modules.auth.dto;

import java.util.Date;

import com.drive.drive.modules.user.entities.UserEntity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDto {
  private String token;
  private Date tokenExpiration;
  private UserEntity user;

}
