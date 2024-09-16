package com.drive.drive.modules.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.drive.drive.security.IsPublic;
import com.drive.drive.modules.auth.dto.LoginDto;
import com.drive.drive.modules.auth.dto.LoginResponseDto;
import com.drive.drive.modules.user.dto.UserDto;
import com.drive.drive.shared.dto.ResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Auth", description = "Endpoints for user authentication.")
public class AuthController {
  @Autowired
  private AuthService authService;

  @Operation(summary = "Register a user")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User registered successfully"),
      @ApiResponse(responseCode = "500", description = "Error saving the user")
  })
  @PostMapping("/register")
  @IsPublic
  public ResponseEntity<ResponseDto<UserDto>> register(@RequestBody UserDto usuarioDTO) {
    log.info("Registering user: {}", usuarioDTO.getUsername());
    var res = authService.register(usuarioDTO);
    return ResponseEntity.status(res.getCode()).body(res);
  }

  @Operation(summary = "Login with user and password")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User logged successfully"),
      @ApiResponse(responseCode = "500", description = "Error on login the user")
  })
  @PostMapping("/login")
  @IsPublic
  public ResponseEntity<ResponseDto<LoginResponseDto>> authenticate(@RequestBody LoginDto loginDTO) {
    log.info("Authenticating user: {}", loginDTO.getLogin());
    var response = authService.authenticateAndSave(loginDTO);
    return ResponseEntity.status(response.getCode()).body(response);
  }
}
