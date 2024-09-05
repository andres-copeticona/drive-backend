package com.drive.drive.user.bl;

import com.drive.drive.security.JwtUtil;
import com.drive.drive.user.dto.RolDto;
import com.drive.drive.user.dto.UserFilter;
import com.drive.drive.user.dto.UsuarioDTO;
import com.drive.drive.user.entity.Rol;
import com.drive.drive.user.entity.Usuario;
import com.drive.drive.user.repository.RolRepository;
import com.drive.drive.user.repository.UserRepository;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

  Logger log = LoggerFactory.getLogger(UserService.class);

  @Autowired
  private UserRepository usuarioRepository;

  @Autowired
  private RolRepository rolRepository;

  @Autowired
  private RestTemplate restTemplate;

  @Value("${external-api.authentication-url}")
  private String authenticationUrl;

  public UsuarioDTO register(UsuarioDTO usuarioDTO) {
    Usuario usuario = convertToEntity(usuarioDTO);

    Rol rolUsuario = rolRepository.findByNombreRol("Administrador")
        .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
    usuario.setRol(rolUsuario);

    Usuario savedUsuario = usuarioRepository.save(usuario);
    return convertToDTO(savedUsuario);
  }

  private Usuario saveNewUser(UsuarioDTO usuarioDTO) {
    Rol rolPorDefecto = rolRepository.findById(2L)
        .orElseThrow(() -> new RuntimeException("Rol por defecto no encontrado"));
    Usuario usuario = convertToEntity(usuarioDTO);
    usuario.setRol(rolPorDefecto);
    usuario.setPassword("ENCRYPTED_PASSWORD"); // Asegúrate de establecer la contraseña correctamente
    return usuarioRepository.save(usuario);
  }

  // private String generateToken(String username) {
  // return Jwts.builder()
  // .setSubject(username)
  // .setIssuedAt(new Date(System.currentTimeMillis()))
  // .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
  // .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
  // .compact();
  // }

  // Método para convertir DTO a entidad
  private Usuario convertToEntity(UsuarioDTO usuarioDTO) {
    Usuario usuario = new Usuario();
    usuario.setIdServidor(usuarioDTO.getIdServidor());
    usuario.setNombres(usuarioDTO.getNombres());
    usuario.setPaterno(usuarioDTO.getPaterno());
    usuario.setMaterno(usuarioDTO.getMaterno());
    usuario.setCelular(usuarioDTO.getCelular());
    usuario.setDomicilio(usuarioDTO.getDomicilio());
    usuario.setCi(usuarioDTO.getCi());
    usuario.setEstado(usuarioDTO.isStatus() ? "Activo" : "Inactivo");
    usuario.setCargo(usuarioDTO.getCargo());
    usuario.setDependencia(usuarioDTO.getDependencia());
    usuario.setSigla(usuarioDTO.getSigla());
    usuario.setUsuario(usuarioDTO.getUsuario());
    usuario.setPassword("ENCRYPTED_PASSWORD"); // Asegúrate de manejar la contraseña correctamente
    usuario.setCreatedAt(new Date());
    usuario
        .setUpdatedAt(usuarioDTO.getUpdatedAt() != null ? java.sql.Timestamp.valueOf(usuarioDTO.getUpdatedAt()) : null);
    usuario.setStatus(usuarioDTO.isStatus());
    usuario.setDeleted(false);

    return usuario;
  }

  public Page<UsuarioDTO> findAll(UserFilter filter) {
    Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());

    Page<Usuario> usuarios;

    // TODO: Change the search query to use the new field
    if (filter.getSearchTerm() != "") {
      usuarios = usuarioRepository
          .findByNombresContainingIgnoreCaseOrPaternoContainingIgnoreCaseOrMaternoContainingIgnoreCase(
              filter.getSearchTerm(), filter.getSearchTerm(), filter.getSearchTerm(), pageable);
    } else {
      usuarios = usuarioRepository.findAll(pageable);
    }

    List<UsuarioDTO> dtos = usuarios.getContent().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
    return new PageImpl<>(dtos, pageable, usuarios.getTotalElements());
  }

  // Método para convertir entidades a DTO
  private UsuarioDTO convertToDTO(Usuario usuario) {
    UsuarioDTO usuarioDTO = new UsuarioDTO();
    usuarioDTO.setUsuarioID(usuario.getUsuarioID());
    usuarioDTO.setNombres(usuario.getNombres());
    usuarioDTO.setPaterno(usuario.getPaterno());
    usuarioDTO.setMaterno(usuario.getMaterno());
    usuarioDTO.setCelular(usuario.getCelular());
    usuarioDTO.setCi(usuario.getCi());
    usuarioDTO.setCargo(usuario.getCargo());
    usuarioDTO.setDependencia(usuario.getDependencia());
    usuarioDTO.setSigla(usuario.getSigla());
    usuarioDTO.setDomicilio(usuario.getDomicilio());
    usuarioDTO.setUsuario(usuario.getUsuario());
    usuarioDTO.setStatus(usuario.isStatus());
    usuarioDTO.setFullName(usuario.getFullname());
    RolDto rolDto = convertToRolDto(usuario.getRol());
    usuarioDTO.setRoles(new HashSet<>(Collections.singletonList(rolDto)));
    return usuarioDTO;
  }

  private RolDto convertToRolDto(Rol rol) {
    RolDto rolDto = new RolDto();
    rolDto.setRolID(rol.getRolID());
    rolDto.setNombreRol(rol.getNombreRol());
    return rolDto;
  }

  @Transactional
  public UsuarioDTO changeUserRole(long userId, long roleId) {
    Usuario usuario = usuarioRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    Rol newRole = rolRepository.findById(roleId)
        .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

    usuario.setRol(newRole);
    usuarioRepository.save(usuario);

    return convertToDTO(usuario);
  }

  public UsuarioDTO findUserById(long userId) {
    Usuario usuario = usuarioRepository.findById(userId)
        .orElse(null);
    return usuario != null ? convertToDTO(usuario) : null;
  }

  @Transactional
  public UsuarioDTO editUserProfile(long userId, UsuarioDTO usuarioDTO) {
    Usuario usuario = usuarioRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    usuario.setNombres(usuarioDTO.getNombres());
    usuario.setPaterno(usuarioDTO.getPaterno());
    usuario.setMaterno(usuarioDTO.getMaterno());
    usuario.setRol(usuarioDTO.getRoles().stream().findFirst().map(this::convertToRolEntity).orElse(null));
    usuario.setCargo(usuarioDTO.getCargo());
    usuario.setCelular(usuarioDTO.getCelular());
    usuario.setDomicilio(usuarioDTO.getDomicilio());
    usuario.setDependencia(usuarioDTO.getDependencia());
    usuario.setSigla(usuarioDTO.getSigla());

    usuarioRepository.save(usuario);

    return convertToDTO(usuario);
  }

  private Rol convertToRolEntity(RolDto rolDto) {
    Rol rol = new Rol();
    rol.setRolID(rolDto.getRolID());
    rol.setNombreRol(rolDto.getNombreRol());
    return rol;
  }

  public List<String> getAllDependencies() {
    return usuarioRepository.findAll().stream()
        .map(Usuario::getDependencia)
        .distinct()
        .collect(Collectors.toList());
  }

  public List<RolDto> getAllRoles() {
    List<Rol> roles = rolRepository.findAll();
    return roles.stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  private RolDto convertToDTO(Rol rol) {
    RolDto rolDto = new RolDto();
    rolDto.setRolID(rol.getRolID());
    rolDto.setNombreRol(rol.getNombreRol());
    return rolDto;
  }
}
