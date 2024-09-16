package com.drive.drive.modules.user.controllers;

import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.ResponseDto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import com.drive.drive.modules.user.services.UserService;
import com.drive.drive.modules.user.dto.*;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/users")
@Tag(name = "User", description = "Endpoints for user management.")
public class UserController {

  @Autowired
  private UserService userService;

  @GetMapping("/")
  public ResponseEntity<ResponseDto<ListResponseDto<List<UserDto>>>> getAllUsers(
      @Parameter(description = "User filter") @Valid @ParameterObject UserFilter filter) {
    log.info("Fetching page {} of users with page size {}", filter);
    var results = userService.findAll(filter);
    return ResponseEntity.status(results.getCode()).body(results);
  }

  @PutMapping("/change-role/{userId}/{roleId}")
  public ResponseEntity<ResponseDto<UserDto>> changeUserRole(@PathVariable long userId, @PathVariable long roleId) {
    log.info("Change user ID: {} to role ID: {}", userId, roleId);
    var res = userService.changeUserRole(userId, roleId);
    return ResponseEntity.status(res.getCode()).body(res);
  }

  @GetMapping("/{userId}")
  public ResponseEntity<ResponseDto<UserDto>> getUserById(@PathVariable long userId) {
    log.info("Fetching user with ID: {}", userId);
    var res = userService.findUserById(userId);
    return ResponseEntity.status(res.getCode()).body(res);
  }

  @PutMapping("/{userId}")
  public ResponseEntity<ResponseDto<UserDto>> editUserProfile(@PathVariable long userId,
      @RequestBody UserDto usuarioDTO) {
    log.info("Update user ID: {}", userId);
    var res = userService.editUserProfile(userId, usuarioDTO);
    return ResponseEntity.status(res.getCode()).body(res);
  }

  @GetMapping("/dependencies")
  public ResponseEntity<ResponseDto<List<String>>> getAllDependencies() {
    List<String> dependencies = userService.getAllDependencies();
    ResponseDto<List<String>> response = new ResponseDto<>(HttpStatus.OK.value(), dependencies,
        "Lista de dependencias obtenida correctamente");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/roles")
  public ResponseEntity<List<RoleDto>> getAllRoles() {
    log.info("Obteniendo todos los roles disponibles");
    try {
      List<RoleDto> roles = userService.getAllRoles();
      return ResponseEntity.ok(roles);
    } catch (Exception e) {
      log.error("Error al obtener los roles", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

}
