package com.drive.drive.modules.user.repositories;

import com.drive.drive.modules.user.entities.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

  Optional<UserEntity> findByUsernameAndDeletedFalse(String user);

  Optional<UserEntity> findById(Long id);

  List<UserEntity> findByDependence(String dependence);
}
