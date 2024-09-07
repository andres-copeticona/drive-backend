package com.drive.drive.modules.user.mappers;

import com.drive.drive.modules.user.dto.RoleDto;
import com.drive.drive.modules.user.entities.RoleEntity;

public class RoleMapper {
  public static RoleDto entityToDto(RoleEntity role) {
    RoleDto dto = new RoleDto();
    dto.setId(role.getId());
    dto.setName(role.getName());
    return dto;
  }

  public static RoleEntity dtoToEntity(RoleDto dto) {
    RoleEntity entity = new RoleEntity();
    entity.setId(dto.getId());
    entity.setName(dto.getName());
    return entity;
  }
}
