package com.drive.drive.sharing.repository;

import com.drive.drive.sharing.entity.SharedFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SharedFolderRepository extends JpaRepository<SharedFolder, Long> {
    List<SharedFolder> findByFolder_Id(Long folderId);
    List<SharedFolder> findByEmisor_UsuarioID(Long usuarioID);
    List<SharedFolder> findByReceptor_UsuarioID(Long usuarioID);

    @Query("SELECT sf FROM SharedFolder sf WHERE sf.receptor.dependencia = ?1")
    List<SharedFolder> findByReceptorDependencia(String dependencia);

    List<SharedFolder> findByReceptor_UsuarioIDAndFolder_Id(Long receptorUserId, Long folderId);
    List<SharedFolder> findByEmisor_UsuarioIDAndFolder_Id(Long emisorUserId, Long folderId);

    @Query("SELECT sf.receptor.usuarioID FROM SharedFolder sf WHERE LOWER(sf.folder.name) = LOWER(?1)")
    List<Long> findUserIdsByFolderName(String folderName);
}
