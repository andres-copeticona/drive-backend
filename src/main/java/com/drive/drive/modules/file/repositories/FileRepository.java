package com.drive.drive.modules.file.repositories;

import com.drive.drive.modules.file.entities.FileEntity;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long>, JpaSpecificationExecutor<FileEntity> {
  // find by user id
  List<FileEntity> findByDeletedFalse();

  Optional<FileEntity> findByCodeAndDeletedFalse(String code);

  List<FileEntity> findByFolder_Id(Long folderId);

  // find by user id
  List<FileEntity> findByUser_idAndFolder_IdAndDeletedFalse(Long userId, Long folderId);

  // find by access type
  List<FileEntity> findByAccessTypeAndDeletedFalse(String accessType);

  List<FileEntity> findByUser_idAndDeletedFalseOrderByCreatedDateDesc(Long userId);

  // fin by categoria and user id los que no esten eleiminados
  List<FileEntity> findByCategoryAndUser_idAndDeletedFalse(String categoria, Long userId);

  // find by user id los que no esten eleiminados
  List<FileEntity> findByUser_idAndDeletedFalse(Long userId);

  // find by folder id and user id los que no esten eleiminados
  List<FileEntity> findByFolder_IdAndDeletedFalse(Long folderId);

  Optional<FileEntity> findByCodeAndAccessTypeAndDeletedFalse(String code, String accessType);

  @Modifying
  @Transactional
  @Query("UPDATE SharedFileEntity dc SET dc.deleted = true where dc.file.id = :id")
  void deleteSharedDocumentsReferencesByDocumentID(Long id);
}
