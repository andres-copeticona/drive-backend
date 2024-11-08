package com.drive.drive.modules.file.entities;

import com.drive.drive.modules.user.entities.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Documentos_Compartidos")
@SQLDelete(sql = "UPDATE Documentos_Compartidos SET deleted = true WHERE CompartidoID = ?")
@SQLRestriction("deleted <> true")
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

  @Column(name = "Deleted")
  private Boolean deleted;
}
