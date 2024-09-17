package com.drive.drive.modules.folder.repositories;

import com.drive.drive.modules.folder.entities.FolderEntity;
import com.drive.drive.modules.user.entities.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<FolderEntity, Long>, JpaSpecificationExecutor<FolderEntity> {

  List<FolderEntity> findByUser(UserEntity user);

  List<FolderEntity> findByUser_idAndParentFolderIsNull(Long userId);

  Optional<FolderEntity> findByCode(String code);

  List<FolderEntity> findByParentFolder_Id(Long parentFolderId);

  List<FolderEntity> findByParentFolderIsNullAndUser_id(Long userId);

  Optional<FolderEntity> findByCodeAndAccessTypeAndDeletedFalse(String code, String accessType);

  List<FolderEntity> findByParentFolder_IdAndDeletedFalse(Long parentFolderId);

  @Modifying
  @Transactional
  @Query("DELETE FROM FileEntity d WHERE d.folder.id = :folderId")
  void deleteDocumentsByFolderId(Long folderId);

  @Modifying
  @Transactional
  void deleteByName(String name);

  List<FolderEntity> findByName(String name);

  @Modifying
  @Transactional
  @Query("UPDATE FolderEntity f SET f.parentFolder = null WHERE f.parentFolder.id = :parentFolderId")
  void clearParentFolderReferences(Long parentFolderId);

  @Modifying
  @Query("DELETE FROM SharedFolderEntity cc WHERE cc.folder.id = :folderId")
  void deleteSharedFolderReferencesByFolderId(Long folderId);

  boolean existsByNameAndParentFolder_IdAndUser_id(String name, Long parentFolderId, Long userId);

}
