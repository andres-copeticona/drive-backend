package com.drive.drive.modules.user.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

import com.drive.drive.modules.file.entities.FileEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "qr_codes")
public class QrCodeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "emisor_id", referencedColumnName = "UsuarioID", nullable = false)
  private UserEntity emitter;

  @Column(name = "mensaje")
  private String message;

  @Column(name = "titulo")
  private String title;

  @Column(name = "fecha_creacion")
  private Date creationDate;

  @Column(name = "codeQr")
  private String codeQr;

  @Column(name = "Visitas")
  private Integer visits;

  @OneToOne
  @JoinColumn(name = "ArchivoID", referencedColumnName = "DocumentoID")
  private FileEntity file;

}
