package com.drive.drive.modules.activities.repositories;

import com.drive.drive.modules.activities.entities.ActivityEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ActivityRepository
    extends JpaRepository<ActivityEntity, Long>, JpaSpecificationExecutor<ActivityEntity> {
  List<ActivityEntity> findByUser_id(Long id);
}
