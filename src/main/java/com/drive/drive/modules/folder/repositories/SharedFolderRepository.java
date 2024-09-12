package com.drive.drive.modules.folder.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.drive.drive.modules.folder.entities.SharedFolderEntity;

public interface SharedFolderRepository
    extends JpaRepository<SharedFolderEntity, Long>, JpaSpecificationExecutor<SharedFolderEntity> {

  List<SharedFolderEntity> findByFolder_Id(Long folderId);

  List<SharedFolderEntity> findByEmisor_id(Long id);

  List<SharedFolderEntity> findByReceptor_id(Long id);

  @Query("SELECT sf FROM SharedFolderEntity sf WHERE sf.receptor.dependence = ?1")
  List<SharedFolderEntity> findByReceptorDependencia(String dependencia);

  List<SharedFolderEntity> findByReceptor_idAndFolder_Id(Long receptorUserId, Long folderId);

  List<SharedFolderEntity> findByEmisor_idAndFolder_Id(Long emisorUserId, Long folderId);

  @Query("SELECT sf.receptor.id FROM SharedFolderEntity sf WHERE LOWER(sf.folder.name) = LOWER(?1)")
  List<Long> findUserIdsByFolderName(String folderName);

  @Query("SELECT sf.receptor.id FROM SharedFolderEntity sf WHERE sf.folder.id = ?1")
  List<Long> findUserIdsByFolderId(Long folderId);
}
