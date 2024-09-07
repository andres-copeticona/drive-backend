package com.drive.drive.modules.auth;

import com.drive.drive.security.JwtUtil;
import com.drive.drive.shared.dto.ResponseDto;

import lombok.extern.slf4j.Slf4j;

import com.drive.drive.modules.user.dto.UserDto;
import com.drive.drive.modules.user.entities.RoleEntity;
import com.drive.drive.modules.user.entities.UserEntity;
import com.drive.drive.modules.user.mappers.UserMapper;
import com.drive.drive.modules.user.repositories.RoleRepository;
import com.drive.drive.modules.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import java.util.*;

@Slf4j
@Service
public class AuthService {

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  public ResponseDto<UserDto> register(UserDto usuarioDTO) {
    try {
      UserEntity usuario = UserMapper.createDtoToEntity(usuarioDTO);

      RoleEntity rolUsuario = roleRepository.findByName("Administrador").get();
      usuario.setRole(rolUsuario);

      UserEntity savedUser = userRepository.save(usuario);
      return new ResponseDto<>(201, UserMapper.entityToDto(savedUser), "UserEntity registrado correctamente");
    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseDto<>(500, null, "Error al guardar el usuario");
    }

  }

  public ResponseEntity<Map<String, Object>> authenticateAndSave(String login, String password, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    Map<String, String> map = new HashMap<>();
    map.put("login", login);
    map.put("password", password);
    map.put("token", token);
    // HttpEntity<Map<String, String>> entity = new HttpEntity<>(map, headers);

    UserEntity user = null;

    // Make a mock for the authentication response
    Optional<UserEntity> existingUsuario = userRepository.findById(4L);

    if (existingUsuario.isPresent()) {
      user = existingUsuario.get();
    }

    if (user != null) {
      Map<String, Object> claims = new HashMap<>();
      claims.put("id", user.getId());
      claims.put("rolId", user.getRole().getId());
      String jwtToken = jwtUtil.generateToken(claims, user.getUsername());
      Map<String, Object> result = new HashMap<>();
      result.put("token", jwtToken);
      result.put("user", UserMapper.entityToDto(user));
      return ResponseEntity.ok(result);
    } else {
      throw new RestClientException("Failed to authenticate user with external API");
    }

    // ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
    // authenticationUrl,
    // HttpMethod.POST,
    // entity,
    // new ParameterizedTypeReference<Map<String, Object>>() {
    // });
    //
    // if (response.getStatusCode() == HttpStatus.OK) {
    // Map<String, Object> responseBody = response.getBody();
    // if (responseBody != null && responseBody.get("data") != null) {
    // List<Map<String, Object>> usersData = (List<Map<String, Object>>)
    // responseBody.get("data");
    // if (!usersData.isEmpty()) {
    // // Suponiendo que siempre es una lista con un solo objeto de usuario
    // Map<String, Object> userData = usersData.get(0);
    // UserDto usuarioDTO = new ObjectMapper().convertValue(userData,
    // UserDto.class);
    //
    // // Verificar si el usuario ya existe en la base de datos
    // Optional<UserEntity> existingUsuario =
    // usuarioRepository.findByUsuario(usuarioDTO.getUsuario());
    // UserEntity usuario;
    // if (existingUsuario.isPresent()) {
    // usuario = existingUsuario.get();
    // } else {
    // // El usuario es nuevo, guarda en la base de datos
    // usuario = saveNewUser(usuarioDTO);
    // }
    //
    // //TODO: Generar el token JWT
    // String jwtToken = ""; //generateToken(usuario.getUsuario());
    //
    // // Preparar la respuesta
    // Map<String, Object> result = new HashMap<>();
    // result.put("token", jwtToken);
    // result.put("user", convertToDTO(usuario));
    //
    // return ResponseEntity.ok(result);
    // }
    // }
    // }
    // throw new RestClientException("Failed to authenticate user with external
    // API");
  }
}
