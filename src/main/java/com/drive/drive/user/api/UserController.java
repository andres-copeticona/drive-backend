package com.drive.drive.user.api;

import com.drive.drive.user.bl.UserService;
import com.drive.drive.user.dto.*;

import org.springframework.data.domain.Page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/users")
public class UserController {

  private static final Logger log = LoggerFactory.getLogger(UserController.class);

  @Autowired
  private UserService userService;

  @GetMapping("/")
  public ResponseEntity<ResponseDto<Page<UsuarioDTO>>> getAllUsers(UserFilter filter) {
    log.info("Fetching page {} of users with page size {}", filter);
    Page<UsuarioDTO> usuarios = userService.findAll(filter);

    ResponseDto<Page<UsuarioDTO>> response = new ResponseDto<>(HttpStatus.OK.value(), usuarios,
        "Users fetched successfully");

    return ResponseEntity.ok(response);
  }

  // obtiene cambios de rol
  @PutMapping("/change-role/{userId}/{roleId}")
  public ResponseEntity<ResponseDto<UsuarioDTO>> changeUserRole(@PathVariable long userId, @PathVariable long roleId) {
    log.info("Cambiando el rol del usuario con ID: {} al rol ID: {}", userId, roleId);
    try {
      UsuarioDTO updatedUserDto = userService.changeUserRole(userId, roleId);
      ResponseDto<UsuarioDTO> response = new ResponseDto<>(HttpStatus.OK.value(), updatedUserDto,
          "Rol de usuario actualizado correctamente");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error al cambiar el rol del usuario con ID: {}", userId, e);
      ResponseDto<UsuarioDTO> response = new ResponseDto<>(HttpStatus.BAD_REQUEST.value(), null, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
  }

  @GetMapping("/{userId}")
  public ResponseEntity<ResponseDto<UsuarioDTO>> getUserById(@PathVariable long userId) {
    log.info("Fetching user with ID: {}", userId);

    try {
      UsuarioDTO userDto = userService.findUserById(userId);
      if (userDto != null) {
        ResponseDto<UsuarioDTO> response = new ResponseDto<>(HttpStatus.OK.value(), userDto,
            "User fetched successfully");
        return ResponseEntity.ok(response);
      } else {
        log.error("User with ID {} not found", userId);
        ResponseDto<UsuarioDTO> response = new ResponseDto<>(HttpStatus.NOT_FOUND.value(), null, "User not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }
    } catch (Exception e) {
      log.error("Error fetching user with ID: {}", userId, e);
      ResponseDto<UsuarioDTO> response = new ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), null,
          "Internal server error");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @PutMapping("/{userId}")
  public ResponseEntity<ResponseDto<UsuarioDTO>> editUserProfile(@PathVariable long userId,
      @RequestBody UsuarioDTO usuarioDTO) {
    log.info("Editando el perfil del usuario con ID: {}", userId);
    try {
      UsuarioDTO updatedUserDto = userService.editUserProfile(userId, usuarioDTO);
      ResponseDto<UsuarioDTO> response = new ResponseDto<>(HttpStatus.OK.value(), updatedUserDto,
          "Perfil de usuario actualizado correctamente");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error al editar el perfil del usuario con ID: {}", userId, e);
      ResponseDto<UsuarioDTO> response = new ResponseDto<>(HttpStatus.BAD_REQUEST.value(), null, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
  }

  @GetMapping("/dependencies")
  public ResponseEntity<ResponseDto<List<String>>> getAllDependencies() {
    List<String> dependencies = userService.getAllDependencies();
    ResponseDto<List<String>> response = new ResponseDto<>(HttpStatus.OK.value(), dependencies,
        "Lista de dependencias obtenida correctamente");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/roles")
  public ResponseEntity<List<RolDto>> getAllRoles() {
    log.info("Obteniendo todos los roles disponibles");
    try {
      List<RolDto> roles = userService.getAllRoles();
      return ResponseEntity.ok(roles);
    } catch (Exception e) {
      log.error("Error al obtener los roles", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

}
