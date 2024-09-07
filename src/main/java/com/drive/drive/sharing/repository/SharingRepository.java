package com.drive.drive.sharing.repository;

import com.drive.drive.sharing.entity.SharedDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharingRepository extends JpaRepository<SharedDocumentEntity, Long> {

  List<SharedDocumentEntity> findByReceptor_id(Long userId);

  List<SharedDocumentEntity> findByEmisor_id(Long userId);

  List<SharedDocumentEntity> findByDocumento_Id(Long documentoId);

  @Query("SELECT s FROM SharedDocumentEntity s WHERE s.emisor.id = :emisorId AND s.receptor.id = :receptorId")
  List<SharedDocumentEntity> findSharedDocumentsBetweenUsers(Long emisorId, Long receptorId);

}
