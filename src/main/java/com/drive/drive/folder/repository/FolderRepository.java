package com.drive.drive.folder.repository;

import com.drive.drive.folder.entity.FolderEntity;
import com.drive.drive.user.entity.Usuario;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FolderRepository extends JpaRepository<FolderEntity, Long> {

    // buscar un usuario
    List<FolderEntity> findByUser(Usuario user);

    //buscar un folder padre
    List<FolderEntity> findByParentFolder_Id(Long parentFolderId);

    // buscar un folder padre que no ete eliminado
    List<FolderEntity> findByParentFolder_IdAndDeletedFalse(Long parentFolderId);

    // eleiminar un folder por  id
    @Modifying
    @Transactional
    @Query("DELETE FROM FileEntity d WHERE d.folder.id = :folderId")
    void deleteDocumentsByFolderId(Long folderId);

    @Modifying
    @Transactional
    void deleteByName(String name);

    // buscar por el nombre
    List<FolderEntity> findByName(String name);

    // limpiar las referencias padre
    @Modifying
    @Transactional
    @Query("UPDATE FolderEntity f SET f.parentFolder = null WHERE f.parentFolder.id = :parentFolderId")
    void clearParentFolderReferences(Long parentFolderId);

    // modifcar y borrar las referencias por folder
    @Modifying
    @Query("DELETE FROM SharedFolder cc WHERE cc.folder.id = :folderId")
    void deleteSharedFolderReferencesByFolderId(Long folderId);

    // verificar si existe ese folder del usaurio
    boolean existsByNameAndParentFolder_IdAndUser_UsuarioID(String name, Long parentFolderId, Long userId);


}
