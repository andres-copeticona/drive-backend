package com.drive.drive.user.api;

import com.drive.drive.user.dto.*;
import org.springframework.data.domain.Page;

import com.drive.drive.security.IsPublic;
import com.drive.drive.user.bl.AuthService;
import com.drive.drive.user.entity.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private static final Logger log = LoggerFactory.getLogger(AuthController.class);

  @Autowired
  private AuthService authService;

  // regitra un usuario
  @PostMapping("/register")
  public ResponseEntity<ResponseDto<UsuarioDTO>> register(@RequestBody UsuarioDTO usuarioDTO) {
    log.info("Registering user: {}", usuarioDTO.getUsuario());
    UsuarioDTO resultDTO = authService.register(usuarioDTO);
    ResponseDto<UsuarioDTO> response = new ResponseDto<>(HttpStatus.OK.value(), resultDTO,
        "User registered successfully");
    return ResponseEntity.ok(response);
  }

  // autentica un usuario
  @PostMapping("/login")
  @IsPublic
  public ResponseEntity<Map<String, Object>> authenticate(@RequestBody LoginDTO loginDTO) {
    try {
      // Llama al método de autenticación en AuthService que retorna el token y el
      // usuario
      ResponseEntity<Map<String, Object>> response = authService.authenticateAndSave(
          loginDTO.getLogin(),
          loginDTO.getPassword(),
          loginDTO.getToken());

      // Retorna la respuesta con el token y la información del usuario
      return ResponseEntity.ok(response.getBody());
    } catch (RestClientException e) {
      // Manejo de errores en caso de fallo en la autenticación
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
  }

  // obtiene todos los usuarios
  @GetMapping("/users")
  public ResponseEntity<ResponseDto<Page<UsuarioDTO>>> getAllUsersPaginated(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size) {
    log.info("Fetching page {} of users with page size {}", page, size);
    Page<UsuarioDTO> usuarios = authService.findAllUsersPaginated(page, size);
    ResponseDto<Page<UsuarioDTO>> response = new ResponseDto<>(HttpStatus.OK.value(), usuarios,
        "Users fetched successfully");
    return ResponseEntity.ok(response);
  }

  // obtiene cambios de rol
  @PutMapping("/change-role/{userId}/{roleId}")
  public ResponseEntity<ResponseDto<UsuarioDTO>> changeUserRole(@PathVariable long userId, @PathVariable long roleId) {
    log.info("Cambiando el rol del usuario con ID: {} al rol ID: {}", userId, roleId);
    try {
      UsuarioDTO updatedUserDto = authService.changeUserRole(userId, roleId);
      ResponseDto<UsuarioDTO> response = new ResponseDto<>(HttpStatus.OK.value(), updatedUserDto,
          "Rol de usuario actualizado correctamente");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error al cambiar el rol del usuario con ID: {}", userId, e);
      ResponseDto<UsuarioDTO> response = new ResponseDto<>(HttpStatus.BAD_REQUEST.value(), null, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
  }

  // obtiene un usuario por su id
  @GetMapping("/user/{userId}")
  public ResponseEntity<ResponseDto<UsuarioDTO>> getUserById(@PathVariable long userId) {
    log.info("Fetching user with ID: {}", userId);
    try {
      UsuarioDTO userDto = authService.findUserById(userId);
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

  // edita el perfil de un usuario
  @PutMapping("/edit-profile/{userId}")
  public ResponseEntity<ResponseDto<UsuarioDTO>> editUserProfile(@PathVariable long userId,
      @RequestBody UsuarioDTO usuarioDTO) {
    log.info("Editando el perfil del usuario con ID: {}", userId);
    try {
      UsuarioDTO updatedUserDto = authService.editUserProfile(userId, usuarioDTO);
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
    List<String> dependencies = authService.getAllDependencies();
    ResponseDto<List<String>> response = new ResponseDto<>(HttpStatus.OK.value(), dependencies,
        "Lista de dependencias obtenida correctamente");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/roles")
  public ResponseEntity<List<RolDto>> getAllRoles() {
    log.info("Obteniendo todos los roles disponibles");
    try {
      List<RolDto> roles = authService.getAllRoles();
      return ResponseEntity.ok(roles);
    } catch (Exception e) {
      log.error("Error al obtener los roles", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

}
