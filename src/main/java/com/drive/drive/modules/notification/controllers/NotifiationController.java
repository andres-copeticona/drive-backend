package com.drive.drive.modules.notification.controllers;

import com.drive.drive.modules.notification.dto.AllNotificationDto;
import com.drive.drive.modules.notification.dto.NotificationDto;
import com.drive.drive.modules.notification.dto.NotificationFilter;
import com.drive.drive.modules.notification.services.NotificationService;
import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.ResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/notificaciones")
public class NotifiationController {

  @Autowired
  private NotificationService notificationService;

  @Operation(summary = "List activities")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "List of activities fetched successfully."),
      @ApiResponse(responseCode = "500", description = "Error fetching the activities list.")
  })
  @GetMapping("/")
  public ResponseEntity<ResponseDto<ListResponseDto<List<NotificationDto>>>> listActivities(
      @Parameter(description = "Notification filter") @ParameterObject NotificationFilter filter) {
    var results = notificationService.listActivities(filter);
    return ResponseEntity.status(results.getCode()).body(results);
  }

  @Operation(summary = "Check to Read notification")
  @PostMapping("/{notificacionId}/read")
  public ResponseEntity<ResponseDto<Boolean>> checkRead(@PathVariable Long notificacionId) {
    var res = notificationService.checkNotification(notificacionId);
    return ResponseEntity.status(res.getCode()).body(res);
  }

  @Operation(summary = "Send notification to all users")
  @PostMapping("/all")
  public ResponseEntity<ResponseDto<Boolean>> notifyAll(@Valid @RequestBody AllNotificationDto allNotification) {
    var res = notificationService.notifyToAllUsers(allNotification);
    return ResponseEntity.status(res.getCode()).body(res);
  }
}
