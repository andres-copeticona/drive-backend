package com.drive.drive.auth;

import com.drive.drive.security.JwtUtil;
import com.drive.drive.user.dto.RolDto;
import com.drive.drive.user.dto.UsuarioDTO;
import com.drive.drive.user.entity.Rol;
import com.drive.drive.user.entity.Usuario;
import com.drive.drive.user.repository.RolRepository;
import com.drive.drive.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import java.util.*;

@Service
public class AuthService {

  Logger log = LoggerFactory.getLogger(AuthService.class);

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RolRepository rolRepository;

  public UsuarioDTO register(UsuarioDTO usuarioDTO) {
    Usuario usuario = convertToEntity(usuarioDTO);

    Rol rolUsuario = rolRepository.findByNombreRol("Administrador")
        .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
    usuario.setRol(rolUsuario);

    Usuario savedUsuario = userRepository.save(usuario);
    return convertToDTO(savedUsuario);
  }

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

  public ResponseEntity<Map<String, Object>> authenticateAndSave(String login, String password, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    Map<String, String> map = new HashMap<>();
    map.put("login", login);
    map.put("password", password);
    map.put("token", token);
    HttpEntity<Map<String, String>> entity = new HttpEntity<>(map, headers);

    Usuario user = null;

    // Make a mock for the authentication response
    Optional<Usuario> existingUsuario = userRepository.findById(4L);

    if (existingUsuario.isPresent()) {
      user = existingUsuario.get();
    }

    if (user != null) {
      Map<String, Object> claims = new HashMap<>();
      claims.put("id", user.getUsuarioID());
      claims.put("rolId", user.getRol().getRolID());
      String jwtToken = jwtUtil.generateToken(claims, user.getUsuario());
      Map<String, Object> result = new HashMap<>();
      result.put("token", jwtToken);
      result.put("user", convertToDTO(user));
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
    // UsuarioDTO usuarioDTO = new ObjectMapper().convertValue(userData,
    // UsuarioDTO.class);
    //
    // // Verificar si el usuario ya existe en la base de datos
    // Optional<Usuario> existingUsuario =
    // usuarioRepository.findByUsuario(usuarioDTO.getUsuario());
    // Usuario usuario;
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
}
