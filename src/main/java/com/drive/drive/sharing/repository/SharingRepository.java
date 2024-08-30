package com.drive.drive.sharing.repository;

import com.drive.drive.sharing.entity.SharedDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharingRepository extends JpaRepository<SharedDocumentEntity, Long> {

    List<SharedDocumentEntity> findByReceptor_UsuarioID(Long userId);

    List<SharedDocumentEntity> findByEmisor_UsuarioID(Long userId);

    List<SharedDocumentEntity> findByDocumento_Id(Long documentoId);

    @Query("SELECT s FROM SharedDocumentEntity s WHERE s.emisor.usuarioID = :emisorId AND s.receptor.usuarioID = :receptorId")
    List<SharedDocumentEntity> findSharedDocumentsBetweenUsers(Long emisorId, Long receptorId);

}
