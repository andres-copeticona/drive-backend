package com.drive.drive.modules.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
  private Long id;
  private String idServer;
  private String username;
  private String ci;
  private String names;
  private String firstSurname;
  private String secondSurname;
  private String cellphone;
  private String address;
  private String position;
  private String dependence;
  private String acronym;
  private String fullName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private boolean deleted;
  private boolean status;
  private RoleDto role;

  @JsonSetter("status")
  public void setStatusFromString(String status) {
    this.status = "success".equals(status);
  }
}
