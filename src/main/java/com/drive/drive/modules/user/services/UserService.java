package com.drive.drive.modules.user.services;

import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.ResponseDto;
import com.drive.drive.modules.user.dto.RoleDto;
import com.drive.drive.modules.user.dto.UserFilter;
import com.drive.drive.modules.user.dto.UserDto;
import com.drive.drive.modules.user.entities.RoleEntity;
import com.drive.drive.modules.user.entities.UserEntity;
import com.drive.drive.modules.user.mappers.RoleMapper;
import com.drive.drive.modules.user.mappers.UserMapper;
import com.drive.drive.modules.user.repositories.RoleRepository;
import com.drive.drive.modules.user.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  // @Autowired
  // private RestTemplate restTemplate;

  @Value("${external-api.authentication-url}")
  private String authenticationUrl;

  // public UserDto register(UserDto usuarioDTO) {
  // UserEntity user = UserMapper.createDtoToEntity(usuarioDTO);
  //
  // RoleEntity rolUsuario = roleRepository.findByName("Administrador").get();
  // user.setRole(rolUsuario);
  //
  // UserEntity savedUsuario = usuarioRepository.save(user);
  // return UserMapper.entityToDto(savedUsuario);
  // }

  // private UserEntity saveNewUser(UserDto usuarioDTO) {
  // RoleEntity rolPorDefecto = roleRepository.findById(2L)
  // .orElseThrow(() -> new RuntimeException("RoleEntity por defecto no
  // encontrado"));
  // UserEntity usuario = UserMapper.createDtoToEntity(usuarioDTO);
  // usuario.setRole(rolPorDefecto);
  // usuario.setPassword("ENCRYPTED_PASSWORD"); // Asegúrate de establecer la
  // contraseña correctamente
  // return usuarioRepository.save(usuario);
  // }

  // private String generateToken(String username) {
  // return Jwts.builder()
  // .setSubject(username)
  // .setIssuedAt(new Date(System.currentTimeMillis()))
  // .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
  // .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
  // .compact();
  // }

  public ResponseDto<ListResponseDto<List<UserDto>>> findAll(UserFilter filter) {
    var res = new ResponseDto<ListResponseDto<List<UserDto>>>().setCode(200);
    try {
      Specification<UserEntity> spec = filter.getSpecification();
      Sort sort = filter.getSort();
      Pageable pageable = filter.getPageable();

      List<UserEntity> users;
      Long total = 0L;

      if (pageable == null) {
        users = userRepository.findAll(spec, sort);
        total = Long.valueOf(users.size());
      } else {
        var page = userRepository.findAll(spec, pageable);
        users = page.getContent();
        total = page.getTotalElements();
      }

      List<UserDto> dtos = users.stream().map(UserMapper::entityToDto).collect(Collectors.toList());
      return res.setData(new ListResponseDto<>(dtos, total)).setMessage("Lista de carpetas obtenida correctamente.");
    } catch (Exception e) {
      log.error(e.getMessage());
      return res.setCode(500).setMessage("Error obteniendo la lista de carpetas");
    }
  }

  @Transactional
  public ResponseDto<UserDto> changeUserRole(long userId, long roleId) {
    var res = new ResponseDto<UserDto>().setCode(200);
    try {
      UserEntity user = userRepository.findById(userId).get();
      RoleEntity newRole = roleRepository.findById(roleId).get();
      user.setRole(newRole);
      userRepository.save(user);

      return res.setData(UserMapper.entityToDto(user)).setMessage("Rol actualizado correctamente");
    } catch (Exception e) {
      log.error(e.getMessage());
      return res.setCode(500).setMessage("Error al actualizar el rol");
    }
  }

  public ResponseDto<UserDto> findUserById(long userId) {
    var res = new ResponseDto<UserDto>().setCode(200);
    try {
      Optional<UserEntity> user = userRepository.findById(userId);

      if (!user.isPresent())
        return res.setCode(404).setMessage("Usuario no encontrado");

      return res.setData(UserMapper.entityToDto(user.get())).setMessage("Usuario encontrado");
    } catch (Exception e) {
      log.error(e.getMessage());
      return res.setCode(500).setMessage("Usuario no encontrado");
    }
  }

  @Transactional
  public ResponseDto<UserDto> editUserProfile(long userId, UserDto userDto) {
    try {
      userRepository.findById(userId).get();
      UserEntity user = UserMapper.createDtoToEntity(userDto);
      user.setId(userId);
      userRepository.save(user);

      return new ResponseDto<>(201, UserMapper.entityToDto(user), "Usuario creado correctamente");
    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseDto<>(500, null, "Error al crear el usuario");
    }
  }

  public List<String> getAllDependencies() {
    return userRepository.findAll().stream()
        .map(UserEntity::getDependence)
        .distinct()
        .collect(Collectors.toList());
  }

  public List<RoleDto> getAllRoles() {
    List<RoleEntity> roles = roleRepository.findAll();
    return roles.stream()
        .map(RoleMapper::entityToDto)
        .collect(Collectors.toList());
  }
}
