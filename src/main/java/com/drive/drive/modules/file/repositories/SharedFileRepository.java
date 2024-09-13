package com.drive.drive.modules.file.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.drive.drive.modules.file.entities.SharedFileEntity;

import java.util.List;

@Repository
public interface SharedFileRepository
    extends JpaRepository<SharedFileEntity, Long>, JpaSpecificationExecutor<SharedFileEntity> {

  List<SharedFileEntity> findByReceptor_id(Long userId);

  List<SharedFileEntity> findByEmisor_id(Long userId);

  List<SharedFileEntity> findByFile_Id(Long documentoId);

  // @Query("SELECT s FROM SharedFileEntity WHERE s.emisor.id = :emisorId AND
  // s.receptor.id = :receptorId")
  // List<SharedFileEntity> findSharedDocumentsBetweenUsers(Long emisorId, Long
  // receptorId);

}
