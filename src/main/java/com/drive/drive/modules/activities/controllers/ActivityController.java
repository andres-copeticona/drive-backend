package com.drive.drive.modules.activities.controllers;

import com.drive.drive.modules.activities.dto.ActivityDto;
import com.drive.drive.modules.activities.dto.ActivityFilter;
import com.drive.drive.modules.activities.services.ActivityService;
import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.ResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/activities")
public class ActivityController {

  private final ActivityService activityService;

  @Autowired
  public ActivityController(ActivityService activityService) {
    this.activityService = activityService;
  }

  @Operation(summary = "List activities")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "List of activities fetched successfully."),
      @ApiResponse(responseCode = "500", description = "Error fetching the activities list.")
  })
  @GetMapping("/")
  public ResponseEntity<ResponseDto<ListResponseDto<List<ActivityDto>>>> listActivities(
      @Parameter(description = "Activities filter") @ParameterObject ActivityFilter filter) {
    var results = activityService.listActivities(filter);
    return ResponseEntity.status(results.getCode()).body(results);
  }

  // Obtener actividad por id
  // @GetMapping("/usuario/{usuarioId}/contador")
  // public ResponseEntity<List<ContadorActividadDto>>
  // obtenerActividadesPorUsuario(@PathVariable Long usuarioId) {
  // List<ContadorActividadDto> conatadorActividades =
  // activityService.obtenerContadorActividadesPorUsuario(usuarioId);
  // return new ResponseEntity<>(conatadorActividades, HttpStatus.OK);
  // }
}
