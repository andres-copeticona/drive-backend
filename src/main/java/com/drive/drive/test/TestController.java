package com.drive.drive.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.drive.drive.security.AccessUser;
import com.drive.drive.security.IsPublic;
import com.drive.drive.security.UserData;

@RestController
@RequestMapping("/test")
public class TestController {

  @GetMapping("/hello")
  public String hello(@AccessUser UserData userData) {
    return "hello, " + userData.getUsername();
  }

  @GetMapping("/public")
  @IsPublic
  public String publicPage() {
    return "public";
  }
}
