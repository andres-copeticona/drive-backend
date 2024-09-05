package com.drive.drive.auth;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import com.drive.drive.security.IsPublic;
import com.drive.drive.user.dto.LoginDTO;
import com.drive.drive.user.dto.ResponseDto;
import com.drive.drive.user.dto.UsuarioDTO;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

  private static final Logger log = LoggerFactory.getLogger(AuthController.class);

  @Autowired
  private AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<ResponseDto<UsuarioDTO>> register(@RequestBody UsuarioDTO usuarioDTO) {
    log.info("Registering user: {}", usuarioDTO.getUsuario());
    UsuarioDTO resultDTO = authService.register(usuarioDTO);
    ResponseDto<UsuarioDTO> response = new ResponseDto<>(HttpStatus.OK.value(), resultDTO,
        "User registered successfully");
    return ResponseEntity.ok(response);
  }

  @PostMapping("/login")
  @IsPublic
  public ResponseEntity<Map<String, Object>> authenticate(@RequestBody LoginDTO loginDTO) {
    try {
      ResponseEntity<Map<String, Object>> response = authService.authenticateAndSave(
          loginDTO.getLogin(),
          loginDTO.getPassword(),
          loginDTO.getToken());
      return ResponseEntity.ok(response.getBody());
    } catch (RestClientException e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
  }
}
