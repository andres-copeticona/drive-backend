package com.drive.drive.modules.folder.controllers;

import com.drive.drive.modules.folder.services.FolderService;
import com.drive.drive.modules.folder.dto.CreateFolderDto;
import com.drive.drive.modules.folder.dto.FolderDto;
import com.drive.drive.modules.folder.dto.FolderFilter;
import com.drive.drive.security.AccessUser;
import com.drive.drive.security.UserData;
import com.drive.drive.shared.dto.DownloadDto;
import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
      @ApiResponse(responseCode = "500", description = "Error fetching folder list.")
  })
  @GetMapping("/")
  public ResponseEntity<ResponseDto<ListResponseDto<List<FolderDto>>>> listFolders(
      @Parameter(description = "Folder filter") @ParameterObject FolderFilter filter) {
    log.info("Fetching list of folders.");
    var results = folderService.listFolders(filter);
    return ResponseEntity.status(results.getCode()).body(results);
  }

  @Operation(summary = "Create folder")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Folder created successfully."),
      @ApiResponse(responseCode = "500", description = "Error creating folder.")
  })
  @PostMapping("/")
  public ResponseEntity<ResponseDto<Boolean>> createFolder(
      @AccessUser UserData userData,
      @Valid @RequestBody CreateFolderDto createFolderDto) {
    log.info("Creating folder '{}' for user ID {}.", createFolderDto.getName(), userData.getUserId());
    createFolderDto.setIdUser(userData.getUserId());
    var res = folderService.createFolder(createFolderDto);
    return ResponseEntity.status(res.getCode()).body(res);
  }

  // @Operation(summary = "Share a folder")
  // @ApiResponses(value = {
  // @ApiResponse(responseCode = "200", description = "Folder shared
  // successfully."),
  // @ApiResponse(responseCode = "500", description = "Error sharing folder")
  // })
  // @PostMapping("/share")
  // public ResponseEntity<ResponseDto<Boolean>> share(
  // @AccessUser UserData userData,
  // @Valid @RequestBody ShareFolderDto shareFolderDto) {
  // log.info("Sharing folder '{}' with user ID {}.",
  // shareFolderDto.getFolderId(), userData.getReceptorId());
  // shareFolderDto.setEmisorId(userData.getUserId());
  // var res = folderService.share(shareFolderDto);
  // return ResponseEntity.status(res.getCode()).body(res);
  // }

  @Operation(summary = "Get breadcrumb for a folder")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Breadcrumb fetched successfully."),
      @ApiResponse(responseCode = "500", description = "Error fetching breadcrumb.")
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
      @ApiResponse(responseCode = "500", description = "Error trying to downlod the folder.")
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
      @ApiResponse(responseCode = "500", description = "Error deleting folder.")
  })
  @DeleteMapping("/{folderId}")
  public ResponseEntity<ResponseDto<Boolean>> deleteFolder(@PathVariable Long folderId) {
    log.info("Deleting Folder '{}'.", folderId);
    var res = folderService.deleteFolder(folderId);
    return ResponseEntity.status(res.getCode()).body(res);
  }

  // @PostMapping("/share")
  // public ResponseEntity<ResponseDto<String>> shareFolder(@RequestBody
  // ShareFolderRequest request) {
  // folderService.shareFolder(request.getFolderId(), request.getEmisorId(),
  // request.getReceptorId());
  // ResponseDto<String> response = new ResponseDto<>(200, null, "Carpeta
  // compartida con éxito.");
  // return ResponseEntity.ok(response);
  // }

  // @GetMapping("/shared/{folderId}")
  // public ResponseEntity<FolderContentsDto> listSharedFolderContents(@AccessUser
  // UserData userData,
  // @PathVariable Long folderId) {
  // FolderContentsDto sharedFolderContents =
  // folderService.listSharedFolderContents(userData.getUserId(), folderId);
  // return ResponseEntity.ok(sharedFolderContents);
  // }

  // @GetMapping("/shared/")
  // @Operation(description = "List shared folders as receptor")
  // public ResponseEntity<ResponseDto<List<FolderDto>>>
  // listSharedFolders(@AccessUser UserData userData) {
  // List<FolderDto> sharedFolders =
  // folderService.listSharedFolders(userData.getUserId());
  // ResponseDto<List<FolderDto>> response = new ResponseDto<>(200, sharedFolders,
  // "Carpetas compartidas.");
  // return ResponseEntity.ok(response);
  // }
  //
  // // TODO: Refactor
  // @PostMapping("/share/all")
  // public ResponseEntity<ResponseDto<String>>
  // shareFolderWithAllUsers(@RequestBody ShareFolderRequest request) {
  // folderService.shareFolderWithAllUsers(request.getFolderId(),
  // request.getEmisorId());
  // ResponseDto<String> response = new ResponseDto<>(200, null, "Carpeta
  // compartida con todos los usuarios.");
  // return ResponseEntity.ok(response);
  // }
  //
  // // Endpoint para compartir una carpeta con usuarios de una dependencia
  // @PostMapping("/share/dependency/{dependencyName}")
  // public ResponseEntity<ResponseDto<String>>
  // shareFolderWithUsersByDependency(@PathVariable String dependencyName,
  // @RequestBody ShareFolderRequest request) {
  // folderService.shareFolderWithUsersByDependency(dependencyName,
  // request.getFolderId(), request.getEmisorId());
  // ResponseDto<String> response = new ResponseDto<>(200, null,
  // "Carpeta compartida con usuarios de la dependencia " + dependencyName + ".");
  // return ResponseEntity.ok(response);
  // }
  //
  // // Endpoint para listar usuarios con acceso a una carpeta compartida
  // @GetMapping("/shared/users/{folderId}")
  // public ResponseEntity<ResponseDto<List<UsuarioDTO>>>
  // listUsersSharedWith(@PathVariable Long folderId) {
  // try {
  // List<UsuarioDTO> users = folderService.listUsersWithAccessToFolder(folderId);
  // log.info("Folder ID {} shared with {} users.", folderId, users.size());
  // return ResponseEntity.ok(new ResponseDto<>(200, users, "Lista de usuarios con
  // acceso a la carpeta."));
  // } catch (Exception e) {
  // log.error("Error listing users with access to folder ID {}: {}", folderId,
  // e.getMessage());
  // return new ResponseEntity<>(
  // new ResponseDto<List<UsuarioDTO>>(500, null, "Error al listar usuarios: " +
  // e.getMessage()),
  // HttpStatus.INTERNAL_SERVER_ERROR);
  // }
  // }
  //
  // // Endpoint para generar enlaces de carpeta compartida
  // @GetMapping("/share-link/{folderId}")
  // public ResponseEntity<Map<String, Object>>
  // generateFolderShareLinks(@PathVariable Long folderId,
  // @RequestParam Long userId) {
  // try {
  // List<String> shareLinks = folderService.generateSharedFolderLinks(folderId,
  // userId);
  // if (shareLinks.isEmpty()) {
  // throw new RuntimeException("No se generaron enlaces para la carpeta.");
  // }
  // Map<String, Object> response = new HashMap<>();
  // response.put("success", true);
  // response.put("shareLinks", shareLinks);
  // return ResponseEntity.ok(response);
  // } catch (RuntimeException e) {
  // Map<String, Object> errorResponse = new HashMap<>();
  // errorResponse.put("success", false);
  // errorResponse.put("message", "Error: " + e.getMessage());
  // return ResponseEntity.badRequest().body(errorResponse);
  // }
  // }
  //
  // // Endpoint para obtener carpetas compartidas por dependencia
  // @GetMapping("/shared/dependency/{dependencyName}")
  // public ResponseEntity<ResponseDto<List<FolderDto>>>
  // getSharedFoldersByDependency(
  // @PathVariable String dependencyName) {
  // List<FolderDto> sharedFolders =
  // folderService.getSharedFoldersByDependency(dependencyName);
  // ResponseDto<List<FolderDto>> response = new ResponseDto<>(200, sharedFolders,
  // "Carpetas compartidas obtenidas por dependencia.");
  // return ResponseEntity.ok(response);
  // }
  //
  // // Endpoint para obtener carpetas compartidas con un usuario específico
  // @GetMapping("/shared/user/{userId}")
  // public ResponseEntity<ResponseDto<List<FolderDto>>>
  // getSharedFoldersWithUser(@PathVariable Long userId) {
  // List<FolderDto> sharedFolders =
  // folderService.getSharedFoldersWithUser(userId);
  // ResponseDto<List<FolderDto>> response = new ResponseDto<>(200, sharedFolders,
  // "Carpetas compartidas con el usuario obtenidas.");
  // return ResponseEntity.ok(response);
  // }
}
