package com.drive.drive.user.repository;
import com.drive.drive.user.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsuarioAndDeletedFalse(String usuario);

    Optional<Usuario> findById(Long id);

    Optional<Usuario> findByUsuario(String usuario);

    List<Usuario> findByDependencia(String dependencia);

}
