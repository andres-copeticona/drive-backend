package com.drive.drive.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.drive.drive.security.IsPublic;

@RestController
@RequestMapping("/api/test")
public class TestController {

  @IsPublic
  @GetMapping("/public")
  public ResponseEntity<String> publicEndpoint() {
    return ResponseEntity.ok("This is a public endpoint");
  }

  @GetMapping("/private")
  public ResponseEntity<String> privateEndpoint(@RequestAttribute("username") String username) {
    return ResponseEntity.ok("This is a private endpoint, Hello " + username);
  }
}
