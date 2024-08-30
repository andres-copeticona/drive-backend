package com.drive.drive.file.api;

import com.drive.drive.file.bl.FileBl;
import com.drive.drive.file.dto.FileDto;
import com.drive.drive.folder.dto.FolderContentsDto;
import com.drive.drive.sharing.dto.SharedDocumentDto;
import com.drive.drive.user.dto.ResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@RestController
@RequestMapping("/file")
public class FileApi {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private FileBl fileBl;

    // subir el archivo
    @PostMapping("/upload/{bucket}")
    public ResponseEntity<ResponseDto<FileDto>> uploadFile(@PathVariable String bucket,
                                                           @RequestParam("file") MultipartFile file,
                                                           @RequestParam("data") String fileDtoStr) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(fileDtoStr);

            FileDto fileDto = new FileDto();
            // Usar métodos seguros para obtener valores de los nodos JSON
            fileDto.setId(jsonNode.has("id") ? jsonNode.get("id").asLong() : null);
            fileDto.setEtag(jsonNode.has("etag") ? jsonNode.get("etag").asText() : null);
            fileDto.setMinioLink(jsonNode.has("minioLink") ? jsonNode.get("minioLink").asText() : null);
            fileDto.setTitle(jsonNode.has("title") ? jsonNode.get("title").asText() : null);
            fileDto.setDescription(jsonNode.has("description") ? jsonNode.get("description").asText() : null);
            fileDto.setFolderId(jsonNode.has("folderId") ? jsonNode.get("folderId").asLong() : null);
            fileDto.setAccessType(jsonNode.has("accessType") ? jsonNode.get("accessType").asText() : null);
            fileDto.setPassword(jsonNode.has("password") ? jsonNode.get("password").asText() : null);
            fileDto.setCreatedDate(new Date());
            fileDto.setModifiedDate(new Date());
            fileDto.setDeleted(false);
            fileDto.setSize(file.getSize());
            fileDto.setUserId(jsonNode.has("userId") ? jsonNode.get("userId").asLong() : null);
            fileDto.setCategoria(jsonNode.has("categoria") ? jsonNode.get("categoria").asText() : null);
            fileDto.setFileType(fileBl.determineFileType(file.getOriginalFilename()));

            log.info("FileDto before upload: {}", fileDto);

            // Llamar a la lógica de negocio para subir el archivo
            FileDto uploadedFileDto = fileBl.uploadFile(fileDto, file);

            log.info("FileDto after upload: {}", uploadedFileDto);

            ResponseDto<FileDto> responseDto = new ResponseDto<>(200, uploadedFileDto, "Archivo subido correctamente");
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage());
            ResponseDto<FileDto> responseDto = new ResponseDto<>(400, null, "Error uploading file: " + e.getMessage());
            return ResponseEntity.badRequest().body(responseDto);
        }
    }




    // compartir el etag del archivo para que el usuario pueda descargarlo
    @GetMapping("/download/{etag}/bucket/{bucket}")
    public ResponseEntity<String> getDownloadUrl(@PathVariable String etag, @PathVariable String bucket) {
        try {
            String url = fileBl.getDownloadUrl(etag, bucket);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            log.error("Error getting file URL: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener la URL del archivo: " + e.getMessage());
        }
    }

    // compartir el archivo
    @PostMapping("/share")
    public ResponseEntity<Map<String, String>> shareFile(@RequestBody SharedDocumentDto sharedDocumentDto) {
        try {
            String sharedDocumentLink = fileBl.shareFile(sharedDocumentDto);
            Map<String, String> response = new HashMap<>();
            response.put("message", "File shared successfully");
            response.put("link", sharedDocumentLink);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sharing file: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error al compartir el archivo: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // listar todos lod documentos
    @GetMapping("/list-all")
    public ResponseEntity<List<FileDto>> listAllFiles() {
        try {
            List<FileDto> files = fileBl.listAllFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            log.error("Error listing files: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // listar los documentos por usuario y carpeta
    @GetMapping("/list/{userId}/{folderId}")
    public ResponseEntity<FolderContentsDto> listFilesByUserAndFolder(@PathVariable Long userId, @PathVariable Long folderId) {
        try {
            FolderContentsDto contents = fileBl.listFilesByUserAndFolder(userId, folderId);
            return ResponseEntity.ok(contents);
        } catch (Exception e) {
            log.error("Error listing files and folders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    // obtener el archivo por id
    @GetMapping("/{id}")
    public ResponseEntity<FileDto> getFileById(@PathVariable Long id) {
        FileDto fileDto = fileBl.getFileById(id);
        if (fileDto != null) {
            return ResponseEntity.ok(fileDto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // obtener los archivos publicos
    @GetMapping("/public-files")
    public ResponseEntity<List<FileDto>> getAllPublicFiles() {
        List<FileDto> files = fileBl.findAllPublicFiles();
        if (!files.isEmpty()) {
            return ResponseEntity.ok(files);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    //mostrar los documentos compartidos por id
    @GetMapping("/shared-documents/{userId}")
    public ResponseEntity<List<SharedDocumentDto>> findAllSharedDocumentsByUserId(@PathVariable Long userId) {
        try {
            List<SharedDocumentDto> sharedDocuments = fileBl.findAllSharedDocumentsByUserId(userId);
            if (sharedDocuments.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return ResponseEntity.ok(sharedDocuments);
            }
        } catch (Exception e) {
            log.error("Error retrieving shared documents: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // eliminar el archivo por id
    @PutMapping("/delete/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable Long fileId) {
        try {
            String message = fileBl.deleteFile(fileId);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la solicitud: " + e.getMessage());
        }
    }

    // obtener los archivos recientes por usuario
    @GetMapping("/recent/{userId}")
    public ResponseEntity<List<FileDto>> getRecentFilesByUser(@PathVariable Long userId) {
        try {
            List<FileDto> recentFiles = fileBl.findRecentFilesByUserId(userId);
            if (recentFiles.isEmpty()) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.ok(recentFiles);
            }
        } catch (Exception e) {
            log.error("Error retrieving recent files: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // obtener los archivos por categoria y usuario
    @GetMapping("/files-by-category")
    public ResponseEntity<List<FileDto>> getFilesByCategoryAndUser(@RequestParam String categoria, @RequestParam Long userId) {
        try {
            List<FileDto> filesByCategoryAndUser = fileBl.findFilesByCategoryAndUser(categoria, userId);
            if (filesByCategoryAndUser.isEmpty()) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.ok(filesByCategoryAndUser);
            }
        } catch (Exception e) {
            log.error("Error retrieving files by category and user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // actualizar la categoria del archivo
    @PutMapping("/{fileId}/category")
    public ResponseEntity<String> updateFileCategory(@PathVariable Long fileId, @RequestBody Map<String, String> categoryRequest) {
        try {
            String newCategory = categoryRequest.get("category");
            String response = fileBl.updateFileCategory(fileId, newCategory);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al procesar la solicitud: " + e.getMessage());
        }
    }

    // obtener el numero de archivos por categoria y usuario
    @GetMapping("/count-categories/{userId}")
    public ResponseEntity<Map<String, Long>> getCountByCategoriesByUser(@PathVariable Long userId) {
        Map<String, Long> categoryCounts = fileBl.countCategoriesByUser(userId);
        if (!categoryCounts.isEmpty()) {
            return ResponseEntity.ok(categoryCounts);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    // obtener los archivos compartidos por usuario
    @GetMapping("/sharedDocumentsUsers/{userId}")
    public ResponseEntity<Map<String, List<String>>> getSharedDocumentsUsersByUserId(@PathVariable Long userId) {
        try {
            Map<String, List<String>> userNames = fileBl.findAllSharedDocumentsUsersByUserId(userId);
            if (userNames.get("sharedWithMe").isEmpty() && userNames.get("iSharedWith").isEmpty()) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.ok(userNames);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // obtener los documentos compartidos entre usuarios
    @GetMapping("/sharedDocuments/emisor/{emisorId}/receptor/{receptorId}")
    public ResponseEntity<List<SharedDocumentDto>> getSharedDocumentsBetweenUsers(@PathVariable Long emisorId, @PathVariable Long receptorId) {
        try {
            List<SharedDocumentDto> sharedDocuments = fileBl.findSharedDocumentsBetweenUsers(emisorId, receptorId);
            if (sharedDocuments.isEmpty()) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.ok(sharedDocuments);
            }
        } catch (Exception e) {
            log.error("Error retrieving shared documents between users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // obtener el almacenamiento utilizado por el usuario
    @GetMapping("/storage/{userId}")
    public ResponseEntity<ResponseDto<Map<String, Object>>> getStorageUsedByUser(@PathVariable Long userId) {
        try {
            Map<String, Object> storageStats  = fileBl.getTotalStorageUsedByUser(userId);
            ResponseDto<Map<String, Object>> response = new ResponseDto<>(HttpStatus.OK.value(), storageStats, "El almacenamiento total utilizado por el usuario se ha recuperado con éxito.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting storage used: {}", e.getMessage());
            ResponseDto<Map<String, Object>> response = new ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, "Error al obtener el almacenamiento utilizado: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}