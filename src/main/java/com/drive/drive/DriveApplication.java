package com.drive.drive;

import org.springframework.boot.SpringApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableTransactionManagement
public class DriveApplication {

  public static void main(String[] args) {
    SpringApplication.run(DriveApplication.class, args);
  }

}
