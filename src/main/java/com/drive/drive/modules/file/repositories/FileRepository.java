package com.drive.drive.modules.file.repositories;

import com.drive.drive.modules.file.entities.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
  // find by user id
  List<FileEntity> findByDeletedFalse();

  // find by user id
  List<FileEntity> findByUser_idAndFolder_IdAndDeletedFalse(Long userId, Long folderId);

  // find by access type
  List<FileEntity> findByAccessTypeAndDeletedFalse(String accessType);

  // find by user id en orden descendente
  List<FileEntity> findByUser_idAndDeletedFalseOrderByCreatedDateDesc(Long userId);

  // fin by categoria and user id los que no esten eleiminados
  List<FileEntity> findByCategoriaAndUser_idAndDeletedFalse(String categoria, Long userId);

  // find by user id los que no esten eleiminados
  List<FileEntity> findByUser_idAndDeletedFalse(Long userId);

  // find by folder id and user id los que no esten eleiminados
  List<FileEntity> findByFolder_IdAndDeletedFalse(Long folderId);

}
