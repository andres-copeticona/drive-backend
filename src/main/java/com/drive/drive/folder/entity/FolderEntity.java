package com.drive.drive.folder.entity;

import com.drive.drive.user.entity.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carpetas")
public class FolderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FolderID")
    private Long id;

    @Column(name = "Nombre")
    private String name;

    @Column(name = "TipoDeAcceso")
    private String accessType;

    @Column(name = "CreatedAt")
    private Date creationDate;

    @Column(name = "UpdatedAt")
    private Date updateDate;

    @Column(name = "Deleted")
    private Boolean deleted;

    @ManyToOne
    @JoinColumn(name = "FolderUsuarioID", referencedColumnName = "UsuarioID", nullable = true)
    private Usuario user;

    // Referencia a la carpeta padre
    @ManyToOne
    @JoinColumn(name = "ParentFolderID", referencedColumnName = "FolderID", nullable = true)
    private FolderEntity parentFolder;
}