package com.drive.drive.security;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Hidden
public class UserData {
  // TODO: add ip
  String username;
  Long userId;
  Long roleId;
}
