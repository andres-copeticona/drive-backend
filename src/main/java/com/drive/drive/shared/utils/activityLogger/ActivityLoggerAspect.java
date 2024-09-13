package com.drive.drive.shared.utils.activityLogger;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;

import com.drive.drive.modules.activities.dto.CreateActivityDto;
import com.drive.drive.modules.activities.services.ActivityService;

import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Component
@Aspect
public class ActivityLoggerAspect {

  private final HttpServletRequest request;
  private final ActivityService activityService;

  public ActivityLoggerAspect(HttpServletRequest request, ActivityService activityService) {
    this.request = request;
    this.activityService = activityService;
  }

  @Pointcut("@annotation(activityLogger)")
  public void activityLoggerPointcut(ActivityLogger activityLogger) {
  }

  @AfterReturning(pointcut = "activityLoggerPointcut(activityLogger)", returning = "result")
  public void logActivity(JoinPoint joinPoint, ActivityLogger activityLogger, Object result) {
    CreateActivityDto dto = new CreateActivityDto();
    String description;
    if (request.getAttribute("log_description") == null)
      description = activityLogger.description();
    else
      description = request.getAttribute("log_description").toString();

    String action;
    if (request.getAttribute("log_action") == null)
      action = activityLogger.action();
    else
      action = request.getAttribute("log_action").toString();

    String ip = request.getHeader("X-FORWARDED-FOR");
    if (ip == null) {
      ip = request.getRemoteAddr();
    }

    dto.setUserId(Long.parseLong(request.getAttribute("userId").toString()));
    dto.setIp(ip);
    dto.setDescription(description);
    dto.setActivityType(action);

    activityService.createActivity(dto);
  }
}
