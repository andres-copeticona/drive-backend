package com.drive.drive.modules.user.mappers;

import java.sql.Timestamp;
import java.util.Date;

import com.drive.drive.modules.auth.dto.GadcLoginResponseDto;
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

    UserDto dto = new UserDto();
    dto.setId(user.getId());
    dto.setNames(user.getNames());
    dto.setFirstSurname(user.getFirstSurname());
    dto.setSecondSurname(user.getSecondSurname());
    dto.setFullName(user.getNames() + " " + user.getFirstSurname() + " " + user.getSecondSurname());
    dto.setCellphone(user.getCellphone());
    dto.setCi(user.getCi());
    dto.setPosition(user.getPosition());
    dto.setDependence(user.getDependence());
    dto.setAcronym(user.getAcronym());
    dto.setAddress(user.getAddress());
    dto.setUsername(user.getUsername());
    dto.setStatus(user.isStatus());

    RoleDto roleDto = new RoleDto();
    roleDto.setId(user.getRole().getId());
    roleDto.setName(user.getRole().getName());
    dto.setRole(roleDto);
    return dto;

  }

  public static UserEntity loginDtoToEntity(GadcLoginResponseDto login) {
    UserEntity user = new UserEntity();
    user.setIdServer(login.getIdServidor());
    user.setNames(login.getNombres());
    user.setFirstSurname(login.getPaterno());
    user.setSecondSurname(login.getMaterno());
    user.setCellphone(login.getCelular());
    user.setAddress(login.getDomicilio());
    user.setCi(login.getCi());
    user.setState(login.isStatus() ? "Activo" : "Inactivo");
    user.setPosition(login.getCargo());
    user.setDependence(login.getDependencia());
    user.setAcronym(login.getSigla());
    user.setUsername(login.getUsuario());
    user.setPassword("ENCRYPTED_PASSWORD");
    user.setCreatedAt(new Date());
    user.setUpdatedAt(login.getUpdatedAt() != null ? java.sql.Timestamp.valueOf(login.getUpdatedAt()) : null);
    user.setStatus(login.isStatus());
    user.setDeleted(false);

    return user;
  }
}
