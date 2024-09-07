package com.drive.drive.sharing.repository;

import com.drive.drive.sharing.entity.SharedFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SharedFolderRepository extends JpaRepository<SharedFolder, Long> {
  List<SharedFolder> findByFolder_Id(Long folderId);

  List<SharedFolder> findByEmisor_id(Long id);

  List<SharedFolder> findByReceptor_id(Long id);

  @Query("SELECT sf FROM SharedFolder sf WHERE sf.receptor.dependence = ?1")
  List<SharedFolder> findByReceptorDependencia(String dependencia);

  List<SharedFolder> findByReceptor_idAndFolder_Id(Long receptorUserId, Long folderId);

  List<SharedFolder> findByEmisor_idAndFolder_Id(Long emisorUserId, Long folderId);

  @Query("SELECT sf.receptor.id FROM SharedFolder sf WHERE LOWER(sf.folder.name) = LOWER(?1)")
  List<Long> findUserIdsByFolderName(String folderName);

  @Query("SELECT sf.receptor.id FROM SharedFolder sf WHERE sf.folder.id = ?1")
  List<Long> findUserIdsByFolderId(Long folderId);
}
