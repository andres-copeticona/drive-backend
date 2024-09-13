package com.drive.drive.modules.file.entities;

import com.drive.drive.modules.user.entities.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Documentos_Compartidos")
public class SharedFileEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "CompartidoID")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "Receptor_UsuarioID", referencedColumnName = "UsuarioID")
  private UserEntity receptor;

  @ManyToOne
  @JoinColumn(name = "Emisor_UsuarioID", referencedColumnName = "UsuarioID")
  private UserEntity emisor;

  @ManyToOne
  @JoinColumn(name = "DocumentoID", referencedColumnName = "DocumentoID")
  private FileEntity file;

  @Column(name = "TipoAcceso")
  private String type;

  @Column(name = "CreatedAt")
  private Date sharedAt;

  @Column(name = "LinkDocumento")
  private String fileLink;
}
