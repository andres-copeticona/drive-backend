package com.drive.drive.folder.api;

import com.drive.drive.folder.bl.FolderBl;
import com.drive.drive.folder.dto.FolderContentsDto;
import com.drive.drive.folder.dto.FolderDto;
import com.drive.drive.folder.dto.ShareFolderRequest;
import com.drive.drive.folder.entity.FolderEntity;
import com.drive.drive.folder.repository.FolderRepository;
import com.drive.drive.user.dto.ResponseDto;
import com.drive.drive.user.dto.UsuarioDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/folder")
public class FolderApi {

    @Autowired
    private FolderBl folderBl;
    @Autowired
    private FolderRepository folderRepository;

    // Endpoint para listar carpetas
    @GetMapping("/list")
    public ResponseDto<List<String>> listFolders() {
        try {
            List<String> results = folderBl.listFolders();
            log.info("List of folders fetched successfully.");
            return new ResponseDto<>(200, results, "List of folders fetched successfully.");
        } catch (Exception e) {
            log.error("Error fetching folder list: {}", e.getMessage());
            return new ResponseDto<>(500, null, "Error fetching folder list: " + e.getMessage());
        }
    }

    // Endpoint para crear una carpeta
    @PostMapping("/create")
    public ResponseDto<String> createFolder(@RequestParam String folderName, @RequestParam Long userId, @RequestParam(required = false) Long parentFolderId) {
        try {
            folderBl.createFolder(folderName, userId, parentFolderId);
            log.info("Folder '{}' created successfully for user ID {}.", folderName, userId);
            return new ResponseDto<>(200, "Folder created successfully!", null);
        } catch (Exception e) {
            log.error("Error creating folder '{}' for user ID {}: {}", folderName, userId, e.getMessage());
            return new ResponseDto<>(500, null, "Error creating folder: " + e.getMessage());
        }
    }

    // Endpoint para eliminar una carpeta
    @DeleteMapping("/delete/{folderName}")
    public ResponseDto<String> deleteFolder(@PathVariable String folderName) {
        try {
            folderBl.deleteFolder(folderName);
            log.info("Folder '{}' deleted successfully.", folderName);
            return new ResponseDto<>(200, "Folder deleted successfully!", null);
        } catch (Exception e) {
            log.error("Error deleting folder '{}': {}", folderName, e.getMessage());
            return new ResponseDto<>(500, null, "Error deleting folder: " + e.getMessage());
        }
    }

    // Endpoint para listar carpetas por usuario
    @GetMapping("/list/{userId}")
    public ResponseDto<List<FolderDto>> listFoldersByUser(@PathVariable Long userId) {
        try {
            List<FolderDto> results = folderBl.listFoldersByUser(userId);
            log.info("List of folders for user ID {} fetched successfully.", userId);
            return new ResponseDto<>(200, results, "List of folders fetched successfully.");
        } catch (Exception e) {
            log.error("Error fetching folder list for user ID {}: {}", userId, e.getMessage());
            return new ResponseDto<>(500, null, "Error fetching folder list for user ID " + userId + ": " + e.getMessage());
        }
    }

    // Endpoint para compartir una carpeta
    @PostMapping("/share")
    public ResponseEntity<ResponseDto<String>> shareFolder(@RequestBody ShareFolderRequest request) {
        folderBl.shareFolder(request.getFolderId(), request.getEmisorId(), request.getReceptorId());
        ResponseDto<String> response = new ResponseDto<>(200, null, "Carpeta compartida con éxito.");
        return ResponseEntity.ok(response);
    }

    // Endpoint para listar contenidos de una carpeta compartida como receptor

    @GetMapping("/shared/{userId}/{folderId}")
    public ResponseEntity<FolderContentsDto> listSharedFolderContents(@PathVariable Long userId, @PathVariable Long folderId) {
        FolderContentsDto sharedFolderContents = folderBl.listSharedFolderContents(userId, folderId);
        return ResponseEntity.ok(sharedFolderContents);
    }

    // Endpoint para listar carpetas compartidas como receptor
    @GetMapping("/shared/{userId}")
    public ResponseEntity<ResponseDto<List<FolderDto>>> listSharedFolders(@PathVariable Long userId) {
        List<FolderDto> sharedFolders = folderBl.listSharedFolders(userId);
        ResponseDto<List<FolderDto>> response = new ResponseDto<>(200, sharedFolders, "Carpetas compartidas.");
        return ResponseEntity.ok(response);
    }

    // Endpoint para compartir una carpeta con todos los usuarios
    @PostMapping("/share/all")
    public ResponseEntity<ResponseDto<String>> shareFolderWithAllUsers(@RequestBody ShareFolderRequest request) {
        folderBl.shareFolderWithAllUsers(request.getFolderId(), request.getEmisorId());
        ResponseDto<String> response = new ResponseDto<>(200, null, "Carpeta compartida con todos los usuarios.");
        return ResponseEntity.ok(response);
    }

    // Endpoint para compartir una carpeta con usuarios de una dependencia
    @PostMapping("/share/dependency/{dependencyName}")
    public ResponseEntity<ResponseDto<String>> shareFolderWithUsersByDependency(@PathVariable String dependencyName, @RequestBody ShareFolderRequest request) {
        folderBl.shareFolderWithUsersByDependency(dependencyName, request.getFolderId(), request.getEmisorId());
        ResponseDto<String> response = new ResponseDto<>(200, null, "Carpeta compartida con usuarios de la dependencia " + dependencyName + ".");
        return ResponseEntity.ok(response);
    }

    // Endpoint para listar usuarios con acceso a una carpeta compartida
    @GetMapping("/shared/users/{folderId}")
    public ResponseEntity<ResponseDto<List<UsuarioDTO>>> listUsersSharedWith(@PathVariable Long folderId) {
        try {
            List<UsuarioDTO> users = folderBl.listUsersWithAccessToFolder(folderId);
            log.info("Folder ID {} shared with {} users.", folderId, users.size());
            return ResponseEntity.ok(new ResponseDto<>(200, users, "Lista de usuarios con acceso a la carpeta."));
        } catch (Exception e) {
            log.error("Error listing users with access to folder ID {}: {}", folderId, e.getMessage());
            return new ResponseEntity<>(new ResponseDto<List<UsuarioDTO>>(500, null, "Error al listar usuarios: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint para generar enlaces de carpeta compartida
    @GetMapping("/share-link/{folderId}")
    public ResponseEntity<Map<String, Object>> generateFolderShareLinks(@PathVariable Long folderId, @RequestParam Long userId) {
        try {
            List<String> shareLinks = folderBl.generateSharedFolderLinks(folderId, userId);
            if (shareLinks.isEmpty()) {
                throw new RuntimeException("No se generaron enlaces para la carpeta.");
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("shareLinks", shareLinks);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Endpoint para obtener carpetas compartidas por dependencia
    @GetMapping("/shared/dependency/{dependencyName}")
    public ResponseEntity<ResponseDto<List<FolderDto>>> getSharedFoldersByDependency(@PathVariable String dependencyName) {
        List<FolderDto> sharedFolders = folderBl.getSharedFoldersByDependency(dependencyName);
        ResponseDto<List<FolderDto>> response = new ResponseDto<>(200, sharedFolders, "Carpetas compartidas obtenidas por dependencia.");
        return ResponseEntity.ok(response);
    }

    // Endpoint para obtener carpetas compartidas con un usuario específico
    @GetMapping("/shared/user/{userId}")
    public ResponseEntity<ResponseDto<List<FolderDto>>> getSharedFoldersWithUser(@PathVariable Long userId) {
        List<FolderDto> sharedFolders = folderBl.getSharedFoldersWithUser(userId);
        ResponseDto<List<FolderDto>> response = new ResponseDto<>(200, sharedFolders, "Carpetas compartidas con el usuario obtenidas.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{bucketName}")
    public ResponseEntity<byte[]> downloadBucketContents(@PathVariable String bucketName) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            folderBl.downloadBucketContents(bucketName, byteArrayOutputStream);

            byte[] zipContent = byteArrayOutputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename(bucketName + ".zip")
                    .build());

            return new ResponseEntity<>(zipContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error downloading bucket '{}': {}", bucketName, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }


}
