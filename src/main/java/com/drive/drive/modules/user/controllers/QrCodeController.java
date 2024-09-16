package com.drive.drive.modules.user.controllers;

import com.drive.drive.modules.user.services.QrCodeService;
import com.drive.drive.security.IsPublic;
import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.ResponseDto;
import com.drive.drive.modules.qr.dto.QrCodeDto;
import com.drive.drive.modules.qr.dto.QrCodeFilter;
import com.drive.drive.modules.user.entities.QrCodeEntity;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/qr")
public class QrCodeController {

  @Autowired
  private QrCodeService qrCodeService;

  @GetMapping("/")
  public ResponseEntity<ResponseDto<ListResponseDto<List<QrCodeDto>>>> listAllQrCodes(
      @ParameterObject QrCodeFilter filter) {
    var res = qrCodeService.listQrCodes(filter);
    return ResponseEntity.status(res.getCode()).body(res);
  }

  // Endpoint para guardar un nuevo código QR
  @PostMapping("/save")
  public ResponseEntity<ResponseDto<QrCodeEntity>> saveQrCode(@RequestBody QrCodeEntity qrCode) {
    try {
      QrCodeEntity savedQrCode = qrCodeService.saveQrCode(qrCode);
      ResponseDto<QrCodeEntity> response = new ResponseDto<>(HttpStatus.CREATED.value(), savedQrCode,
          "Código QR guardado correctamente");
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (Exception e) {
      ResponseDto<QrCodeEntity> response = new ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), null,
          "Error al guardar el código QR: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  // Endpoint para obtener un código QR por su ID
  @GetMapping("/{id}")
  public ResponseEntity<ResponseDto<QrCodeEntity>> getQrCodeById(@PathVariable Long id) {
    Optional<QrCodeEntity> qrCodeOptional = qrCodeService.getQrCodeById(id);
    if (qrCodeOptional.isPresent()) {
      ResponseDto<QrCodeEntity> response = new ResponseDto<>(HttpStatus.OK.value(), qrCodeOptional.get(),
          "Código QR encontrado");
      return ResponseEntity.ok(response);
    } else {
      ResponseDto<QrCodeEntity> response = new ResponseDto<>(HttpStatus.NOT_FOUND.value(), null,
          "Código QR no encontrado");
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
  }

  @GetMapping("/public/{code}")
  @IsPublic
  public ResponseEntity<ResponseDto<QrCodeDto>> getQrCodeById(@PathVariable String code) {
    var res = qrCodeService.getQrCodeByCodeQr(code);
    return ResponseEntity.status(res.getCode()).body(res);

  }
}
