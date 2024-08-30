package com.drive.drive.sharing.entity;

import com.drive.drive.folder.entity.FolderEntity;
import com.drive.drive.user.entity.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "Carpetas_Compartidos")
public class SharedFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private FolderEntity folder;

    @ManyToOne
    @JoinColumn(name = "emisor_id")
    private Usuario emisor;

    @ManyToOne
    @JoinColumn(name = "receptor_id")
    private Usuario receptor;

    @Column(name = "shared_at")
    private Date sharedAt;

    // Constructor, getters y setters
}