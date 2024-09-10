package com.drive.drive.modules.file.controllers;

import com.drive.drive.modules.file.services.FileService;
import com.drive.drive.modules.file.dto.CreateFilesDto;
import com.drive.drive.modules.file.dto.FileDto;
import com.drive.drive.modules.file.dto.FileFilter;
import com.drive.drive.security.AccessUser;
import com.drive.drive.security.UserData;
import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
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
      @ApiResponse(responseCode = "500", description = "Failed to fetch")
  })
  @GetMapping("/")
  public ResponseEntity<ResponseDto<ListResponseDto<List<FileDto>>>> listAllFiles(
      @Valid @Parameter(description = "File filter") @ParameterObject FileFilter filter) {
    log.info("Fetching all files...");
    ResponseDto<ListResponseDto<List<FileDto>>> files = fileService.listAllFiles(filter);
    return ResponseEntity.status(files.getCode()).body(files);
  }

  @Operation(summary = "Get file by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully fetched file by ID"),
      @ApiResponse(responseCode = "500", description = "Failed to fetch the file")
  })
  @GetMapping("/{id}")
  public ResponseEntity<ResponseDto<FileDto>> getFileById(@PathVariable Long id) {
    log.info("Fetching file with id: {}...", id);
    ResponseDto<FileDto> res = fileService.getFileById(id);
    return ResponseEntity.status(res.getCode()).body(res);
  }

  @Operation(summary = "Download file by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully downloaded file by ID"),
      @ApiResponse(responseCode = "500", description = "Failed to download the file")
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

  @Operation(summary = "Upload a file")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully uploaded file"),
      @ApiResponse(responseCode = "500", description = "Failed to upload")
  })
  @PostMapping(path = "/upload", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
  public ResponseEntity<ResponseDto<ListResponseDto<List<FileDto>>>> uploadFiles(@AccessUser UserData userData,
      @Valid @ModelAttribute CreateFilesDto createFilesDto) {
    log.info("Uploading files...");
    var uploadedFiles = fileService.uploadMultipleFiles(userData, createFilesDto);
    return ResponseEntity.status(uploadedFiles.getCode()).body(uploadedFiles);
  }

  @Operation(summary = "Delete a file")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully deleted file"),
      @ApiResponse(responseCode = "500", description = "Failed to delete")
  })
  @DeleteMapping("/{fileId}")
  public ResponseEntity<ResponseDto<Boolean>> deleteFile(@PathVariable Long fileId) {
    log.info("Deleting file with id: {}...", fileId);
    var result = fileService.deleteFile(fileId);
    return ResponseEntity.status(result.getCode()).body(result);
  }

  // // compartir el etag del archivo para que el usuario pueda descargarlo
  // @GetMapping("/download/{etag}/bucket/{bucket}")
  // public ResponseEntity<String> getDownloadUrl(@PathVariable String etag,
  // @PathVariable String bucket) {
  // try {
  // String url = fileBl.getDownloadUrl(etag, bucket);
  // return ResponseEntity.ok(url);
  // } catch (Exception e) {
  // log.error("Error getting file URL: {}", e.getMessage());
  // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
  // .body("Error al obtener la URL del archivo: " + e.getMessage());
  // }
  // }

  // @PostMapping("/share")
  // public ResponseEntity<Map<String, String>> shareFile(@RequestBody
  // SharedDocumentDto sharedDocumentDto) {
  // try {
  // String sharedDocumentLink = fileBl.shareFile(sharedDocumentDto);
  // Map<String, String> response = new HashMap<>();
  // response.put("message", "File shared successfully");
  // response.put("link", sharedDocumentLink);
  // return ResponseEntity.ok(response);
  // } catch (Exception e) {
  // log.error("Error sharing file: {}", e.getMessage());
  // Map<String, String> response = new HashMap<>();
  // response.put("message", "Error al compartir el archivo: " + e.getMessage());
  // return ResponseEntity.badRequest().body(response);
  // }
  // }

  // obtener los archivos publicos
  // @GetMapping("/public-files")
  // public ResponseEntity<List<FileDto>> getAllPublicFiles() {
  // List<FileDto> files = fileBl.findAllPublicFiles();
  // if (!files.isEmpty()) {
  // return ResponseEntity.ok(files);
  // } else {
  // return ResponseEntity.noContent().build();
  // }
  // }

  // mostrar los documentos compartidos por id
  // @GetMapping("/shared-documents/{userId}")
  // public ResponseEntity<List<SharedDocumentDto>>
  // findAllSharedDocumentsByUserId(@PathVariable Long userId) {
  // try {
  // List<SharedDocumentDto> sharedDocuments =
  // fileBl.findAllSharedDocumentsByUserId(userId);
  // if (sharedDocuments.isEmpty()) {
  // return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  // } else {
  // return ResponseEntity.ok(sharedDocuments);
  // }
  // } catch (Exception e) {
  // log.error("Error retrieving shared documents: {}", e.getMessage());
  // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
  // }
  // }

  // obtener los archivos recientes por usuario
  // @GetMapping("/recent/{userId}")
  // public ResponseEntity<List<FileDto>> getRecentFilesByUser(@PathVariable Long
  // userId) {
  // try {
  // List<FileDto> recentFiles = fileBl.findRecentFilesByUserId(userId);
  // if (recentFiles.isEmpty()) {
  // return ResponseEntity.noContent().build();
  // } else {
  // return ResponseEntity.ok(recentFiles);
  // }
  // } catch (Exception e) {
  // log.error("Error retrieving recent files: {}", e.getMessage());
  // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  // }
  // }

  // obtener los archivos por categoria y usuario
  // @GetMapping("/files-by-category")
  // public ResponseEntity<List<FileDto>> getFilesByCategoryAndUser(@RequestParam
  // String categoria,
  // @RequestParam Long userId) {
  // try {
  // List<FileDto> filesByCategoryAndUser =
  // fileBl.findFilesByCategoryAndUser(categoria, userId);
  // if (filesByCategoryAndUser.isEmpty()) {
  // return ResponseEntity.noContent().build();
  // } else {
  // return ResponseEntity.ok(filesByCategoryAndUser);
  // }
  // } catch (Exception e) {
  // log.error("Error retrieving files by category and user: {}", e.getMessage());
  // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  // }
  // }

  // actualizar la categoria del archivo
  // @PutMapping("/{fileId}/category")
  // public ResponseEntity<String> updateFileCategory(@PathVariable Long fileId,
  // @RequestBody Map<String, String> categoryRequest) {
  // try {
  // String newCategory = categoryRequest.get("category");
  // String response = fileBl.updateFileCategory(fileId, newCategory);
  // return ResponseEntity.ok(response);
  // } catch (IllegalArgumentException e) {
  // return ResponseEntity.badRequest().body(e.getMessage());
  // } catch (RuntimeException e) {
  // return ResponseEntity.notFound().build();
  // } catch (Exception e) {
  // return ResponseEntity.internalServerError().body("Error al procesar la
  // solicitud: " + e.getMessage());
  // }
  // }

  // obtener el numero de archivos por categoria y usuario
  // @GetMapping("/count-categories/{userId}")
  // public ResponseEntity<Map<String, Long>>
  // getCountByCategoriesByUser(@PathVariable Long userId) {
  // Map<String, Long> categoryCounts = fileBl.countCategoriesByUser(userId);
  // if (!categoryCounts.isEmpty()) {
  // return ResponseEntity.ok(categoryCounts);
  // } else {
  // return ResponseEntity.noContent().build();
  // }
  // }

  // obtener los archivos compartidos por usuario
  // @GetMapping("/sharedDocumentsUsers/{userId}")
  // public ResponseEntity<Map<String, List<String>>>
  // getSharedDocumentsUsersByUserId(@PathVariable Long userId) {
  // try {
  // Map<String, List<String>> userNames =
  // fileBl.findAllSharedDocumentsUsersByUserId(userId);
  // if (userNames.get("sharedWithMe").isEmpty() &&
  // userNames.get("iSharedWith").isEmpty()) {
  // return ResponseEntity.noContent().build();
  // } else {
  // return ResponseEntity.ok(userNames);
  // }
  // } catch (Exception e) {
  // return ResponseEntity.badRequest().body(null);
  // }
  // }

  // obtener los documentos compartidos entre usuarios
  // @GetMapping("/sharedDocuments/emisor/{emisorId}/receptor/{receptorId}")
  // public ResponseEntity<List<SharedDocumentDto>>
  // getSharedDocumentsBetweenUsers(@PathVariable Long emisorId,
  // @PathVariable Long receptorId) {
  // try {
  // List<SharedDocumentDto> sharedDocuments =
  // fileBl.findSharedDocumentsBetweenUsers(emisorId, receptorId);
  // if (sharedDocuments.isEmpty()) {
  // return ResponseEntity.noContent().build();
  // } else {
  // return ResponseEntity.ok(sharedDocuments);
  // }
  // } catch (Exception e) {
  // log.error("Error retrieving shared documents between users: {}",
  // e.getMessage());
  // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
  // }
  // }

  // obtener el almacenamiento utilizado por el usuario
  // @GetMapping("/storage/{userId}")
  // public ResponseEntity<ResponseDto<Map<String, Object>>>
  // getStorageUsedByUser(@PathVariable Long userId) {
  // try {
  // Map<String, Object> storageStats = fileBl.getTotalStorageUsedByUser(userId);
  // ResponseDto<Map<String, Object>> response = new
  // ResponseDto<>(HttpStatus.OK.value(), storageStats,
  // "El almacenamiento total utilizado por el usuario se ha recuperado con
  // éxito.");
  // return new ResponseEntity<>(response, HttpStatus.OK);
  // } catch (Exception e) {
  // log.error("Error getting storage used: {}", e.getMessage());
  // ResponseDto<Map<String, Object>> response = new
  // ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), null,
  // "Error al obtener el almacenamiento utilizado: " + e.getMessage());
  // return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  // }
  // }

}
