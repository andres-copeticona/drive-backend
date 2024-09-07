package com.drive.drive.modules.user.mappers;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import com.drive.drive.modules.user.dto.RoleDto;
import com.drive.drive.modules.user.dto.UserDto;
import com.drive.drive.modules.user.entities.UserEntity;

public class UserMapper {

  public static UserEntity createDtoToEntity(UserDto userDto) {
    return new UserEntity()
        .setIdServer(userDto.getIdServer())
        .setNames(userDto.getNames())
        .setFirstSurname(userDto.getFirstSurname())
        .setSecondSurname(userDto.getSecondSurname())
        .setCellphone(userDto.getCellphone())
        .setAddress(userDto.getAddress())
        .setCi(userDto.getCi())
        .setState(userDto.isStatus() ? "Activo" : "Inactivo")
        .setPosition(userDto.getPosition())
        .setDependence(userDto.getDependence())
        .setAcronym(userDto.getAcronym())
        .setUsername(userDto.getUsername())
        .setPassword("ENCRYPTED_PASSWORD")
        .setCreatedAt(new Date())
        .setUpdatedAt(userDto.getUpdatedAt() != null ? Timestamp.valueOf(userDto.getUpdatedAt()) : null)
        .setStatus(userDto.isStatus())
        .setDeleted(false);
  }

  public static UserDto entityToDto(UserEntity user) {

    UserDto usuarioDTO = new UserDto();
    usuarioDTO.setId(user.getId());
    usuarioDTO.setNames(user.getNames());
    usuarioDTO.setFirstSurname(user.getFirstSurname());
    usuarioDTO.setSecondSurname(user.getSecondSurname());
    usuarioDTO.setCellphone(user.getCellphone());
    usuarioDTO.setCi(user.getCi());
    usuarioDTO.setPosition(user.getPosition());
    usuarioDTO.setDependence(user.getDependence());
    usuarioDTO.setAcronym(user.getAcronym());
    usuarioDTO.setAddress(user.getAddress());
    usuarioDTO.setUsername(user.getUsername());
    usuarioDTO.setStatus(user.isStatus());

    RoleDto rolDto = new RoleDto();
    rolDto.setId(user.getRole().getId());
    rolDto.setName(user.getRole().getName());
    usuarioDTO.setRoles(new HashSet<>(Collections.singletonList(rolDto)));
    return usuarioDTO;

  }
}
