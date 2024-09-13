package com.drive.drive.modules.activities.entities;

import com.drive.drive.modules.user.entities.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "actividades")
public class ActivityEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "nombre")
  private String name;

  @Column(name = "fecha")
  private LocalDateTime date;

  @Column(name = "ip")
  private String ip;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "UsuarioID")
  private UserEntity user;

  @Column(name = "tipo_actividad")
  private String activityType;

  @PrePersist
  protected void onCreate() {
    this.date = LocalDateTime.now();
  }
}
