package com.drive.drive.modules.folder.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.drive.drive.modules.folder.dto.ResponseShareFolderDto;
import com.drive.drive.modules.folder.dto.ShareFolderDto;
import com.drive.drive.modules.folder.dto.ShareFolderFilter;
import com.drive.drive.modules.folder.services.ShareFolderService;
import com.drive.drive.security.AccessUser;
import com.drive.drive.security.UserData;
import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.ResponseDto;
import com.drive.drive.shared.utils.activityLogger.ActivityLogger;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/v1/share-folders")
public class ShareFolderController {
  @Autowired
  private ShareFolderService shareFolderService;

  @GetMapping("/")
  public ResponseEntity<ResponseDto<ListResponseDto<List<ResponseShareFolderDto>>>> list(
      @Parameter(description = "Filter to apply to the list") @Valid ShareFolderFilter filter) {
    log.info("List shared folders");
    var res = shareFolderService.list(filter);
    return ResponseEntity.status(res.getCode()).body(res);
  }

  @PostMapping("/")
  @ActivityLogger(description = "Compartir carpeta", action = "Compartir")
  public ResponseEntity<ResponseDto<Boolean>> share(
      @AccessUser UserData user,
      @RequestBody ShareFolderDto shareFolderDto) {
    log.info("Share folder {}, with {}", shareFolderDto.getId(), shareFolderDto.getReceptorIds());
    shareFolderDto.setEmisorId(user.getUserId());
    var res = shareFolderService.share(shareFolderDto);
    return ResponseEntity.status(res.getCode()).body(res);
  }
}
