package com.drive.drive.modules.file.controllers;

import com.drive.drive.modules.file.services.FileService;
import com.drive.drive.modules.file.dto.*;
import com.drive.drive.security.AccessUser;
import com.drive.drive.security.IsPublic;
import com.drive.drive.security.UserData;
import com.drive.drive.shared.dto.ErrorResponseDto;
import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.QrDto;
import com.drive.drive.shared.dto.ResponseDto;
import com.drive.drive.shared.utils.activityLogger.ActivityLogger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/files")
@Tag(name = "File", description = "Endpoints for file operations")
public class FileController {

  @Autowired
  private FileService fileService;

  @Operation(summary = "List all files")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully fetched all files"),
      @ApiResponse(responseCode = "500", description = "Failed to fetch files", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @GetMapping("/")
  public ResponseEntity<ResponseDto<ListResponseDto<List<FileDto>>>> listAllFiles(
      @Valid @Parameter(description = "File filter") @ParameterObject FileFilter filter) {
    log.info("Fetching all files...");
    ResponseDto<ListResponseDto<List<FileDto>>> files = fileService.listAllFiles(filter);
    return ResponseEntity.status(files.getCode()).body(files);
  }

  @GetMapping("/public")
  @IsPublic
  public ResponseEntity<ResponseDto<ListResponseDto<List<FileDto>>>> listAllPublicFiles(
      @Valid @Parameter(description = "File filter") @ParameterObject FileFilter filter) {
    log.info("Fetching all files...");
    filter.setAccessType("publico");
    ResponseDto<ListResponseDto<List<FileDto>>> files = fileService.listAllFiles(filter);
    return ResponseEntity.status(files.getCode()).body(files);
  }

  @Operation(summary = "Get file by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully fetched file by ID"),
      @ApiResponse(responseCode = "500", description = "Failed to fetch the file", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))

  })
  @GetMapping("/{id}")
  public ResponseEntity<ResponseDto<FileDto>> getFileById(@PathVariable Long id) {
    log.info("Fetching file with id: {}...", id);
    ResponseDto<FileDto> res = fileService.getFileById(id);
    return ResponseEntity.status(res.getCode()).body(res);
  }

  @Operation(summary = "Get qrCode for file by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully fetched qrCode"),
      @ApiResponse(responseCode = "500", description = "Failed to fetch the qrCode", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @GetMapping("/{id}/qr-code")
  public ResponseEntity<ResponseDto<String>> getQrCode(@PathVariable Long id) {
    log.info("Fetching qrCode for file with id: {}...", id);
    ResponseDto<String> res = fileService.getQrCode(id);
    return ResponseEntity.status(res.getCode()).body(res);
  }

  @Operation(summary = "Get file by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully fetched file by ID"),
      @ApiResponse(responseCode = "500", description = "Failed to fetch the file", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @GetMapping("/public/{code}")
  @IsPublic
  public ResponseEntity<ResponseDto<FileDto>> getPublicFile(@PathVariable String code) {
    log.info("Fetching file with code: {}...", code);
    ResponseDto<FileDto> res = fileService.getPublicFileByCode(code);
    return ResponseEntity.status(res.getCode()).body(res);
  }

  @Operation(summary = "Download file by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully downloaded file by ID"),
      @ApiResponse(responseCode = "500", description = "Failed to download the file", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @GetMapping("/{id}/download")
  public ResponseEntity<byte[]> download(@PathVariable Long id) {
    log.info("Download file with id: {}...", id);
    var res = fileService.download(id);
    if (res == null)
      return ResponseEntity.status(500).body(null);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + res.getName());
    return new ResponseEntity<>(res.getData(), headers, HttpStatus.OK);
  }

  @Operation(summary = "Public Download file by code")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully downloaded file by code"),
      @ApiResponse(responseCode = "500", description = "Failed to download the file", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @GetMapping("/public/{code}/download")
  @IsPublic
  public ResponseEntity<byte[]> publicDownload(@PathVariable String code) {
    log.info("Download file with code: {}...", code);
    var res = fileService.publicDownload(code);
    if (res == null)
      return ResponseEntity.status(500).body(null);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + res.getName());
    return new ResponseEntity<>(res.getData(), headers, HttpStatus.OK);
  }

  @Operation(summary = "Check password of file")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Valid password"),
      @ApiResponse(responseCode = "403", description = "Invalid password"),
      @ApiResponse(responseCode = "500", description = "Error checking password")
  })
  @PostMapping("/{id}/check-password")
  public ResponseEntity<ResponseDto<Boolean>> checkPassword(
      @PathVariable Long id,
      @Valid @RequestBody CheckPasswordDto checkPasswordDto) {
    log.info("Check password for file with id: {}...", id);
    checkPasswordDto.setFileId(id);
    var res = fileService.checkPassword(checkPasswordDto);
    return ResponseEntity.status(res.getCode()).body(res);
  }

  @Operation(summary = "Upload a file")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully uploaded file"),
      @ApiResponse(responseCode = "500", description = "Failed to upload", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @PostMapping(path = "/upload", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
  @ActivityLogger(description = "Subiendo archivo", action = "Subir")
  public ResponseEntity<ResponseDto<ListResponseDto<List<FileDto>>>> uploadFiles(@AccessUser UserData userData,
      @Valid @ModelAttribute CreateFilesDto createFilesDto) {
    log.info("Uploading files...");
    var uploadedFiles = fileService.uploadMultipleFiles(userData, createFilesDto);
    return ResponseEntity.status(uploadedFiles.getCode()).body(uploadedFiles);
  }

  @Operation(summary = "Delete a file")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully deleted file"),
      @ApiResponse(responseCode = "500", description = "Failed to delete", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @DeleteMapping("/{fileId}")
  @ActivityLogger(description = "Eliminando archivo", action = "Eliminar")
  public ResponseEntity<ResponseDto<Boolean>> deleteFile(@PathVariable Long fileId) {
    log.info("Deleting file with id: {}...", fileId);
    var result = fileService.deleteFile(fileId);
    return ResponseEntity.status(result.getCode()).body(result);
  }

  @Operation(summary = "Get usage storage")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully fetched usage storage"),
      @ApiResponse(responseCode = "500", description = "Failed to get usage storage", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @GetMapping("/usage-storage")
  public ResponseEntity<ResponseDto<UsageStorageDto>> getUsageStorage(
      @Parameter(description = "User ID") @RequestParam Long userId) {
    log.info("get disk usage for user: {}...", userId);
    var result = fileService.getUsageStorage(userId);
    return ResponseEntity.status(result.getCode()).body(result);
  }

  @Operation(summary = "Upload the signed file")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully uploaded file"),
      @ApiResponse(responseCode = "500", description = "Failed to upload", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  @PostMapping(path = "/sign")
  @ActivityLogger(description = "Firmando un documento", action = "Firma")
  public ResponseEntity<ResponseDto<QrDto>> signFile(@AccessUser UserData userData,
      @Valid @ModelAttribute SignFileDto signFileDto) {
    log.info("Sign files...");
    signFileDto.setUserId(userData.getUserId());
    var uploadedFiles = fileService.signFile(signFileDto);
    return ResponseEntity.status(uploadedFiles.getCode()).body(uploadedFiles);
  }
}
