package com.drive.drive.modules.notification.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import com.drive.drive.modules.user.entities.UserEntity;

@Data
@Entity
@Table(name = "notificaciones")
public class NotificationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "UsuarioID")
  private UserEntity user;

  @Column(name = "titulo", nullable = false, length = 255)
  private String title;

  @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
  private String message;

  @Column(name = "fecha", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private LocalDateTime date = LocalDateTime.now();

  @Column(name = "leido", nullable = false)
  private Boolean read = false;

  @Column(name = "tipo", nullable = false, length = 255)
  private String type;
}
