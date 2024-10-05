package com.drive.drive.modules.folder.controllers;

import com.drive.drive.modules.folder.services.FolderService;
import com.drive.drive.modules.folder.dto.CreateFolderDto;
import com.drive.drive.modules.folder.dto.FolderDto;
import com.drive.drive.modules.folder.dto.FolderFilter;
import com.drive.drive.security.AccessUser;
import com.drive.drive.security.IsPublic;
import com.drive.drive.security.UserData;
import com.drive.drive.shared.dto.DownloadDto;
import com.drive.drive.shared.dto.ErrorResponseDto;
import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.ResponseDto;
import com.drive.drive.shared.utils.activityLogger.ActivityLogger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/folders")
@Tag(name = "Folder", description = "Endpoints for folder management.")
public class FolderController {

  @Autowired
  private FolderService folderService;

  @Operation(summary = "List folders")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "List of folders fetched successfully."),
      @ApiResponse(responseCode = "500", description = "Error fetching folder list.", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @GetMapping("/")
  public ResponseEntity<ResponseDto<ListResponseDto<List<FolderDto>>>> listFolders(
      @Parameter(description = "Folder filter") @ParameterObject FolderFilter filter) {
    log.info("Fetching list of folders.");
    var results = folderService.listFolders(filter);
    return ResponseEntity.status(results.getCode()).body(results);
  }

  @GetMapping("/public")
  @IsPublic
  public ResponseEntity<ResponseDto<ListResponseDto<List<FolderDto>>>> listFoldersForPublic(
      @Parameter(description = "Folder filter") @ParameterObject FolderFilter filter) {
    log.info("Fetching list of folders.");
    filter.setAccessType("publico");
    var results = folderService.listFolders(filter);
    return ResponseEntity.status(results.getCode()).body(results);
  }

  @Operation(summary = "Get folder by code")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "successfully fetching folder"),
      @ApiResponse(responseCode = "500", description = "Error fetching folder.", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @GetMapping("/public/{code}")
  @IsPublic
  public ResponseEntity<ResponseDto<FolderDto>> getPublicFolderByCode(
      @Parameter(description = "Folder code") @PathVariable String code) {
    log.info("Fetching folder by code '{}'.", code);
    var results = folderService.getPublicFolderByCode(code);
    return ResponseEntity.status(results.getCode()).body(results);
  }

  @Operation(summary = "Create folder")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Folder created successfully."),
      @ApiResponse(responseCode = "500", description = "Error creating folder.", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @PostMapping("/")
  @ActivityLogger(description = "Creó una carpeta nueva", action = "Crear")
  public ResponseEntity<ResponseDto<Boolean>> createFolder(
      @AccessUser UserData userData,
      @Valid @RequestBody CreateFolderDto createFolderDto, HttpServletRequest request) {
    log.info("Creating folder '{}' for user ID {}.", createFolderDto.getName(), userData.getUserId());
    createFolderDto.setIdUser(userData.getUserId());
    var res = folderService.createFolder(createFolderDto);
    if (res.getCode() == 200)
      request.setAttribute("log_description", "Creó la carpeta: " + createFolderDto.getName());
    return ResponseEntity.status(res.getCode()).body(res);
  }

  @Operation(summary = "Get breadcrumb for a folder")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Breadcrumb fetched successfully."),
      @ApiResponse(responseCode = "500", description = "Error fetching breadcrumb.", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @GetMapping("/{id}/breadcrumb")
  public ResponseEntity<ResponseDto<List<FolderDto>>> getBreadcrumb(
      @Parameter(description = "ID of the folder") @PathVariable Long id) {
    ResponseDto<List<FolderDto>> response = folderService.getBreadcrumb(id);
    return ResponseEntity.status(response.getCode()).body(response);
  }

  @Operation(summary = "Download a folder")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Folder downloaded successfully."),
      @ApiResponse(responseCode = "500", description = "Error trying to downlod the folder.", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @GetMapping("/{id}/download")
  public ResponseEntity<byte[]> downloadBucketContents(
      @Parameter(description = "ID of the folder") @PathVariable Long id) {
    DownloadDto res = folderService.download(id);

    if (res == null)
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDisposition(ContentDisposition.builder("attachment")
        .filename(res.getName())
        .build());
    return new ResponseEntity<>(res.getData(), headers, HttpStatus.OK);

  }

  @Operation(summary = "Delete a folder")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Folder deleted successfully."),
      @ApiResponse(responseCode = "500", description = "Error deleting folder.", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @DeleteMapping("/{folderId}")
  @ActivityLogger(description = "Eliminó una carpeta", action = "Eliminar")
  public ResponseEntity<ResponseDto<Boolean>> deleteFolder(@PathVariable Long folderId, HttpServletRequest request) {
    log.info("Deleting Folder '{}'.", folderId);
    var res = folderService.deleteFolder(folderId, request);
    return ResponseEntity.status(res.getCode()).body(res);
  }

  @GetMapping("/{id}/toggle-privacity")
  public ResponseEntity<ResponseDto<String>> togglePrivacity(@PathVariable Long id) {
    var res = folderService.togglePrivacity(id);
    return ResponseEntity.status(res.getCode()).body(res);
  }
}
