package com.drive.drive.file.entity;

import com.drive.drive.folder.entity.FolderEntity;
import com.drive.drive.user.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "Documentos")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DocumentoID")
    private Long id;

    @Column(name = "Titulo")
    private String title;

    @Column(name = "Descripcion")
    private String description;

    @Column(name = "Etag")
    private String etag;

    @Column(name = "TipoAcceso")
    private String accessType;

    @Column(name = "Password")
    private String password;

    @Column(name = "CreatedDate")
    private Date createdDate;

    @Column(name = "ModifiedDate")
    private Date modifiedDate;

    @Column(name = "Deleted")
    private Boolean deleted;

    @ManyToOne
    @JoinColumn(name = "DocumentoUsuarioID", referencedColumnName = "UsuarioID", nullable = true)
    private Usuario user;

    @ManyToOne
    @JoinColumn(name = "DocumentoFolderID", referencedColumnName = "FolderID", nullable = true)
    private FolderEntity folder;

    @Column(name = "MinioLink")
    private String minioLink;

    @Column(name = "Categoria")
    private String categoria;

    @Column(name = "Size", nullable = false)
    private Long size = 0L;

    @Column(name = "FileType")
    private String fileType;


}