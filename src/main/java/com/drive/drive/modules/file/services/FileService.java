package com.drive.drive.modules.file.services;

import com.drive.drive.modules.file.dto.CheckPasswordDto;
import com.drive.drive.modules.file.dto.CreateFilesDto;
import com.drive.drive.modules.file.dto.FileDto;
import com.drive.drive.modules.file.dto.FileFilter;
import com.drive.drive.modules.file.dto.FileValidator;
import com.drive.drive.modules.file.dto.SignFileDto;
import com.drive.drive.modules.file.dto.UsageStorageDto;
import com.drive.drive.modules.file.entities.FileEntity;
import com.drive.drive.modules.file.entities.SharedFileEntity;
import com.drive.drive.modules.file.mappers.FileMapper;
import com.drive.drive.modules.file.repositories.FileRepository;
import com.drive.drive.modules.file.repositories.SharedFileRepository;
import com.drive.drive.modules.folder.entities.FolderEntity;
import com.drive.drive.modules.folder.repositories.FolderRepository;
import com.drive.drive.security.UserData;
import com.drive.drive.shared.services.MinioService;
import com.drive.drive.shared.services.SendNotificationService;
import com.drive.drive.shared.utils.PasswordUtil;

import lombok.extern.slf4j.Slf4j;

import com.drive.drive.shared.dto.DownloadDto;
import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.QrDto;
import com.drive.drive.shared.dto.ResponseDto;
import com.drive.drive.modules.user.entities.QrCodeEntity;
import com.drive.drive.modules.user.entities.UserEntity;
import com.drive.drive.modules.user.repositories.QrCodeRepository;
import com.drive.drive.modules.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileService {
  @Autowired
  private FileRepository fileRepository;

  @Autowired
  private FolderRepository folderRepository;

  @Autowired
  private SharedFileRepository sharedFileRepository;

  @Autowired
  private SendNotificationService notificationService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private MinioService minioService;

  @Autowired
  private QrCodeRepository qrCodeRepository;

  public ResponseDto<ListResponseDto<List<FileDto>>> uploadMultipleFiles(UserData userData,
      CreateFilesDto createFilesDto) {
    try {
      FileValidator fileValidator = new FileValidator();
      AtomicLong filesSize = new AtomicLong(0L);

      Arrays.asList(createFilesDto.getFiles()).forEach(file -> {
        if (!fileValidator.isValidFile(file))
          throw new IllegalArgumentException("Invalid file type. Only JPG, JPEG, PDF, MP4, and MP3 files are allowed.");
        filesSize.addAndGet(file.getSize());
      });

      Long currentStorage = getUsageStorage(userData.getUserId()).getData().getTotalUsage();

      if (currentStorage + filesSize.get() > FileValidator.maxFileSize)
        return new ResponseDto<>(403, null, "No tienes suficiente espacio de almacenamiento");

      FolderEntity folder = folderRepository.findById(createFilesDto.getFolderId()).get();
      UserEntity user = userRepository.findById(userData.getUserId()).get();
      List<FileDto> fileDtos = new ArrayList<>();

      for (MultipartFile file : createFilesDto.getFiles()) {
        FileEntity fileEntity = FileMapper.createFilesDtoToFileEntity(createFilesDto, file, folder);
        fileEntity.setUser(user);
        fileDtos.add(FileMapper.FileEntityToDto(fileRepository.save(fileEntity)));
        minioService.addObject(fileEntity, folder, file);
      }

      return new ResponseDto<>(201, new ListResponseDto<List<FileDto>>(fileDtos, Long.valueOf(fileDtos.size())),
          "Archivos cargados correctamente");
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseDto<>(500, null, "Error al cargar los archivos");
    }
  }

  public ResponseDto<ListResponseDto<List<FileDto>>> listAllFiles(FileFilter filter) {
    try {
      Specification<FileEntity> spec = filter.getSpecification();
      Sort sort = filter.getSort();
      Pageable pageable = filter.getPageable();

      List<FileEntity> files;
      Long total = 0L;

      if (pageable == null) {
        files = fileRepository.findAll(spec, sort);
        total = Long.valueOf(files.size());
      } else {
        var res = fileRepository.findAll(spec, pageable);
        files = res.getContent();
        total = res.getTotalElements();
      }

      List<FileDto> dtos = files.stream().map(FileMapper::FileEntityToDto).collect(Collectors.toList());
      return new ResponseDto<>(200, new ListResponseDto<>(dtos, total), "Lista de archivos");
    } catch (Exception e) {
      log.error("Error listing files: {}", e.getMessage());
      return new ResponseDto<>(500, null, "Error al listar los archivos");
    }
  }

  public ResponseDto<FileDto> getFileById(Long fileId) {
    ResponseDto<FileDto> res = new ResponseDto<FileDto>().setCode(200);
    try {
      FileEntity file = fileRepository.findById(fileId).get();
      FileDto dto = FileMapper.FileEntityToDto(file);
      dto.setLink(minioService.getDownloadUrl(file.getFolder().getCode(), file.getCode()));

      return res.setData(dto).setMessage("Archivo obtenido correctamente");
    } catch (Exception e) {
      log.error("Error getting file by ID {}: {}", fileId, e.getMessage());
      return res.setCode(500).setMessage("Error al obtener el archivo");
    }
  }

  public ResponseDto<FileDto> getPublicFileByCode(String code) {
    ResponseDto<FileDto> res = new ResponseDto<FileDto>().setCode(200);
    try {
      FileEntity file = fileRepository.findByCodeAndAccessTypeAndDeletedFalse(code, "publico").get();
      file.setVisits(file.getVisits() + 1);
      fileRepository.save(file);

      FileDto dto = FileMapper.FileEntityToDto(file);
      dto.setLink(minioService.getDownloadUrl(file.getFolder().getCode(), file.getCode()));

      return res.setData(dto).setMessage("Archivo obtenido correctamente");
    } catch (Exception e) {
      log.error("Error getting file by code {}: {}", code, e.getMessage());
      return res.setCode(500).setMessage("Error al obtener el archivo");
    }
  }

  public DownloadDto download(Long fileId) {
    try {
      FileEntity file = fileRepository.findById(fileId).get();
      byte[] data = minioService.download(file.getFolder().getCode(), file.getCode()).readAllBytes();
      return new DownloadDto(file.getTitle(), data);
    } catch (Exception e) {
      log.error("Error downloading file by ID {}: {}", fileId, e.getMessage());
      return null;
    }
  }

  public DownloadDto publicDownload(String code) {
    try {
      FileEntity file = fileRepository.findByCodeAndAccessTypeAndDeletedFalse(code, "publico").get();
      byte[] data = minioService.download(file.getFolder().getCode(), file.getCode()).readAllBytes();
      return new DownloadDto(file.getTitle(), data);
    } catch (Exception e) {
      log.error("Error downloading file by code {}: {}", code, e.getMessage());
      return null;
    }
  }

  public ResponseDto<Boolean> checkPassword(CheckPasswordDto checkPasswordDto) {
    try {
      FileEntity file = fileRepository.findById(checkPasswordDto.getFileId()).get();

      if (file.getPassword() == null)
        throw new IllegalArgumentException("El archivo no tiene contraseña");

      if (!PasswordUtil.checkPassword(checkPasswordDto.getPassword(), file.getPassword()))
        return new ResponseDto<>(403, false, "Contraseña incorrecta");

      return new ResponseDto<>(200, true, "Contraseña verificada correctamente");
    } catch (Exception e) {
      log.error("Error trying to check the password for file {}: {}", checkPasswordDto.getFileId(), e.getMessage());
      return new ResponseDto<>(500, false, "Error al verificar la contraseña");
    }
  }

  public ResponseDto<Boolean> deleteFile(Long fileId) {
    try {
      FileEntity file = fileRepository.findById(fileId)
          .orElseThrow(() -> new RuntimeException("File not found"));

      List<SharedFileEntity> sharedDocuments = sharedFileRepository.findByFile_Id(fileId);
      this.notificationService.sendDeleteFileNotification(sharedDocuments, file);
      sharedFileRepository.deleteAll(sharedDocuments);

      if (!sharedDocuments.isEmpty())
        sharedFileRepository.deleteAll(sharedDocuments);

      minioService.deleteObject(file);

      fileRepository.delete(file);

      return new ResponseDto<>(200, true, "Archivo eliminado correctamente");
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseDto<>(500, false, "Error al eliminar el archivo");
    }
  }

  public ResponseDto<UsageStorageDto> getUsageStorage(Long userId) {
    try {
      userRepository.findById(userId).get();
      List<FileEntity> files = fileRepository.findByUser_idAndDeletedFalse(userId);
      UsageStorageDto usageStorageDto = FileMapper.FileEntityToUsageStorageDto(files);

      return new ResponseDto<>(200, usageStorageDto, "Uso de almacenamiento obtenido correctamente");
    } catch (Exception e) {
      log.error("Error getting disk usage for user {}: {}", userId, e.getMessage());
      return new ResponseDto<>(500, null, "Error al obtener el uso de almacenamiento");
    }
  }

  public ResponseDto<QrDto> signFile(SignFileDto signFileDto) {
    try {
      FileEntity file = fileRepository.findById(Long.valueOf(signFileDto.getFileId())).get();
      file.setCategory("Sellado");
      QrCodeEntity qrCode = new QrCodeEntity();
      if (file.getQrCode() != null)
        qrCode = file.getQrCode();
      else {
        qrCode.setVisits(0);
        qrCode.setCreationDate(new Date());
      }
      qrCode.setFile(file);
      qrCode.setEmitter(file.getUser());
      qrCode.setTitle(signFileDto.getTitle());
      qrCode.setMessage(signFileDto.getDescription());
      qrCode.setCodeQr(signFileDto.getQrCode());
      qrCode = qrCodeRepository.save(qrCode);

      file.setQrCode(qrCode);
      fileRepository.save(file);

      minioService.replaceObject(file, file.getFolder(), signFileDto.getFile());

      QrDto qrDto = new QrDto();
      qrDto.setFile(FileMapper.FileEntityToDto(file));
      qrDto.setQrCode(qrCode.getCodeQr());

      qrDto.getFile().setLink(minioService.getDownloadUrl(file.getFolder().getCode(), file.getCode()));

      return new ResponseDto<>(200, qrDto, "Archivo firmado correctamente");
    } catch (Exception e) {
      log.error("Error signing file: {}", e.getMessage());
      return new ResponseDto<>(500, null, "Error al firmar el archivo");

    }
  }

  public ResponseDto<String> getQrCode(Long id) {
    try {
      QrCodeEntity qr = qrCodeRepository.findById(id).get();
      return new ResponseDto<>(200, qr.getCodeQr(), "Código QR obtenido correctamente");
    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseDto<>(500, null, "Error al obtener el código QR");
    }
  }
}
