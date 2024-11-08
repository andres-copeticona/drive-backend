package com.drive.drive.modules.user.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Getter
@Setter
@Entity
@Table(name = "Rol")
@SQLDelete(sql = "UPDATE Rol SET deleted = true WHERE rolID = ?")
@SQLRestriction("deleted <> true")
public class RoleEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "rolID")
  private Long id;

  @Column(name = "NombreRol", nullable = false)
  private String name;

  @Column(name = "deleted")
  private Boolean deleted;

  @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonBackReference
  private Set<UserEntity> users = new HashSet<>();
}
