package com.drive.drive.modules.activities.mappers;

import com.drive.drive.modules.activities.dto.ActivityDto;
import com.drive.drive.modules.activities.dto.CreateActivityDto;
import com.drive.drive.modules.activities.entities.ActivityEntity;
import com.drive.drive.modules.user.mappers.UserMapper;

public class ActivityMapper {
  public static ActivityEntity createDtoToEntity(CreateActivityDto createActivityDto) {
    ActivityEntity activityEntity = new ActivityEntity();
    activityEntity.setName(createActivityDto.getDescription());
    activityEntity.setIp(createActivityDto.getIp());
    activityEntity.setActivityType(createActivityDto.getActivityType());
    return activityEntity;
  }

  public static ActivityDto entityToDto(ActivityEntity activityEntity) {
    ActivityDto activityDto = new ActivityDto();
    activityDto.setDescription(activityEntity.getName());
    activityDto.setIp(activityEntity.getIp());
    activityDto.setActivityType(activityEntity.getActivityType());
    activityDto.setUser(UserMapper.entityToDto(activityEntity.getUser()));
    return activityDto;
  }
}
