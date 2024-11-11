package com.drive.drive.modules.folder.entities;

import com.drive.drive.modules.user.entities.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "Carpetas_Compartidos")
@SQLDelete(sql = "UPDATE Carpetas_Compartidos SET deleted = true WHERE id = ?")
@SQLRestriction("deleted <> true")
public class SharedFolderEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "folder_id")
  private FolderEntity folder;

  @ManyToOne
  @JoinColumn(name = "emisor_id")
  private UserEntity emisor;

  @ManyToOne
  @JoinColumn(name = "receptor_id")
  private UserEntity receptor;

  @Column(name = "tipo")
  private String type;

  @Column(name = "shared_at")
  private Date sharedAt;

  @Column(name = "deleted", nullable = false)
  @ColumnDefault(value = "false")
  private Boolean deleted;

}
