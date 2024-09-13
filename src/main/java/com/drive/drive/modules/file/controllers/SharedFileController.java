package com.drive.drive.modules.file.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.drive.drive.modules.file.dto.CreateSharedFileDto;
import com.drive.drive.modules.file.dto.SharedFileDto;
import com.drive.drive.modules.file.dto.SharedFileFilter;
import com.drive.drive.modules.file.services.ShareFileService;
import com.drive.drive.security.AccessUser;
import com.drive.drive.security.UserData;
import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.ResponseDto;
import com.drive.drive.shared.utils.activityLogger.ActivityLogger;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/v1/share-files")
public class SharedFileController {
  @Autowired
  private ShareFileService sharedFileService;

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
      @RequestBody CreateSharedFileDto createSharedFileDto) {
    log.info("Share folder {}, with {}", createSharedFileDto.getId(), createSharedFileDto.getReceptorIds());
    createSharedFileDto.setEmisorId(user.getUserId());
    var res = sharedFileService.share(createSharedFileDto);
    return ResponseEntity.status(res.getCode()).body(res);
  }
}
