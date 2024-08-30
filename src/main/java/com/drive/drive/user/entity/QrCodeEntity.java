package com.drive.drive.user.entity;

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
    private String emisor;

    @Column(name = "mensaje")
    private String mensaje;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "fecha_creacion")
    private Date fechaCreacion;

    @Column(name = "codeQr")
    private String codeQr;

}