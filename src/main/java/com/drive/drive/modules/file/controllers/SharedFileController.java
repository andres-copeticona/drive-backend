package com.drive.drive.modules.file.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.drive.drive.modules.file.dto.CreateSharedFileDto;
import com.drive.drive.modules.file.dto.SharedFileDto;
import com.drive.drive.modules.file.dto.SharedFileFilter;
import com.drive.drive.modules.file.services.ShareFileService;
import com.drive.drive.security.AccessUser;
import com.drive.drive.security.UserData;
import com.drive.drive.shared.dto.ErrorResponseDto;
import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.ResponseDto;
import com.drive.drive.shared.utils.activityLogger.ActivityLogger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/v1/share-files")
@Tag(name = "SharedFile", description = "Endpoints for shared file operations")
public class SharedFileController {

  @Autowired
  private ShareFileService sharedFileService;

  @Operation(summary = "List all shared files")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully fetched all shared files"),
      @ApiResponse(responseCode = "500", description = "Failed to fetch shared files", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @GetMapping("/")
  public ResponseEntity<ResponseDto<ListResponseDto<List<SharedFileDto>>>> list(
      @Parameter(description = "Filter to apply to the list") @Valid SharedFileFilter filter) {
    log.info("List shared folders");
    var res = sharedFileService.list(filter);
    return ResponseEntity.status(res.getCode()).body(res);
  }

  @PostMapping("/")
  @ActivityLogger(description = "Compartir archivo", action = "Compartir")
  public ResponseEntity<ResponseDto<Boolean>> share(
      @AccessUser UserData user,
      @Valid @RequestBody CreateSharedFileDto createSharedFileDto,
      HttpServletRequest request) {
    log.info("Share file {}, with {}", createSharedFileDto.getId(), createSharedFileDto.getReceptorIds());
    createSharedFileDto.setEmisorId(user.getUserId());
    var res = sharedFileService.share(createSharedFileDto, request);
    return ResponseEntity.status(res.getCode()).body(res);
  }

  @PostMapping("/all")
  @ActivityLogger(description = "Compartir archivo con todos los usuarios", action = "Compartir")
  public ResponseEntity<ResponseDto<Boolean>> shareAll(
      @AccessUser UserData user,
      @Valid @RequestBody CreateSharedFileDto createShareFileDto) {
    log.info("Share file {}, with all users", createShareFileDto.getId());
    createShareFileDto.setEmisorId(user.getUserId());
    var res = sharedFileService.shareAll(createShareFileDto);
    return ResponseEntity.status(res.getCode()).body(res);
  }

  @PostMapping("/dependency")
  @ActivityLogger(description = "Compartir archivo con todos los usuarios de una dependencia", action = "Compartir")
  public ResponseEntity<ResponseDto<Boolean>> shareDependency(
      @AccessUser UserData user,
      @Valid @RequestBody CreateSharedFileDto createShareFileDto,
      HttpServletRequest request) {
    log.info("Share file {}, with {}", createShareFileDto.getId(), createShareFileDto.getDependency());
    createShareFileDto.setEmisorId(user.getUserId());
    var res = sharedFileService.shareDependency(createShareFileDto);
    if (res.getCode() == 200)
      request.setAttribute("log_description",
          "Compartir archivo con la dependencia: " + createShareFileDto.getDependency());

    return ResponseEntity.status(res.getCode()).body(res);
  }
}
