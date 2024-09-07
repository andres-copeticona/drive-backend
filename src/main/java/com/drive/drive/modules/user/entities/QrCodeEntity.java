package com.drive.drive.modules.user.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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

  @Column(name = "emisor")
  private String emitter;

  @Column(name = "mensaje")
  private String message;

  @Column(name = "titulo")
  private String title;

  @Column(name = "fecha_creacion")
  private Date creationDate;

  @Column(name = "codeQr")
  private String codeQr;

}
