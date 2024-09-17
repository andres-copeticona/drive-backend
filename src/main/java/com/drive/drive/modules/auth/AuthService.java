package com.drive.drive.modules.auth;

import com.drive.drive.security.JwtUtil;
import com.drive.drive.shared.dto.ResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import com.drive.drive.modules.auth.dto.GadcLoginResponseDto;
import com.drive.drive.modules.auth.dto.LoginDto;
import com.drive.drive.modules.auth.dto.LoginResponseDto;
import com.drive.drive.modules.user.dto.UserDto;
import com.drive.drive.modules.user.entities.RoleEntity;
import com.drive.drive.modules.user.entities.UserEntity;
import com.drive.drive.modules.user.mappers.UserMapper;
import com.drive.drive.modules.user.repositories.RoleRepository;
import com.drive.drive.modules.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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

  @Value("${external-api.authentication-url}")
  private String authenticationUrl;

  @Value("${external-api.token}")
  private String token;

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

  public ResponseDto<LoginResponseDto> authenticateAndSave(LoginDto loginDto) {
    var res = new ResponseDto<LoginResponseDto>().setCode(200);

    try {
      var response = loginToGadc(loginDto);

      if (response.getStatusCode() != HttpStatus.OK)
        throw new RestClientException("Failed to authenticate user with external API");

      Map<String, Object> responseBody = response.getBody();

      if (responseBody == null || responseBody.get("data") == null)
        throw new RestClientException("Failed to parse data from external API");

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> usersData = (List<Map<String, Object>>) responseBody.get("data");

      if (usersData.isEmpty())
        throw new RestClientException("Failed to authenticate user with out data");

      Map<String, Object> userData = usersData.get(0);
      GadcLoginResponseDto loginResponseDto = new ObjectMapper().convertValue(userData,
          GadcLoginResponseDto.class);

      Optional<UserEntity> existingUsuario = userRepository
          .findByUsernameAndDeletedFalse(loginResponseDto.getUsuario());
      UserEntity user;

      if (existingUsuario.isPresent()) {
        user = existingUsuario.get();
      } else {
        RoleEntity rolUsuario = roleRepository.findById(1L).get();
        UserEntity userEntity = UserMapper.loginDtoToEntity(loginResponseDto);
        userEntity.setRole(rolUsuario);
        user = userRepository.save(userEntity);
      }

      Map<String, Object> claims = new HashMap<>();
      claims.put("id", user.getId());
      claims.put("rolId", user.getRole().getId());

      String jwtToken = jwtUtil.generateToken(claims, user.getUsername());
      Date expirationDate = jwtUtil.getExpirationDateFromToken(jwtToken);
      LoginResponseDto resp = new LoginResponseDto(jwtToken, expirationDate, user);
      return res.setData(resp).setMessage("Usuario autenticado correctamente");

    } catch (Exception e) {
      log.error(e.getMessage());
      return res.setCode(500).setMessage("Error al autenticar el usuario");
    }

  }

  private ResponseEntity<Map<String, Object>> loginToGadc(LoginDto loginDto) {
    if (loginDto == null || loginDto.getLogin() == null || loginDto.getPassword() == null) {
      throw new IllegalArgumentException("LoginDto y sus propiedades no pueden ser nulos");
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, String> map = new HashMap<>();
    map.put("login", loginDto.getLogin());
    map.put("password", loginDto.getPassword());
    map.put("token", token);

    HttpEntity<Map<String, String>> entity = new HttpEntity<>(map, headers);

    var restTemplate = new RestTemplate();

    try {
      return restTemplate.exchange(authenticationUrl, HttpMethod.POST, entity,
          new ParameterizedTypeReference<Map<String, Object>>() {
          });
    } catch (Exception e) {
      throw e;
    }
  }
}
