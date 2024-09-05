package com.drive.drive;

import org.springframework.boot.SpringApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableTransactionManagement
public class DriveApplication {

  public static void main(String[] args) {
    SpringApplication.run(DriveApplication.class, args);
  }

  @Bean
  public OpenAPI customOpenApi() {
    return new OpenAPI()
        .addSecurityItem(new SecurityRequirement().addList("bearer-key"))
        .components(
            new Components()
                .addSecuritySchemes("bearer-key",
                    new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
        .info(
            new Info()
                .title("Drive API")
                .version("1.0.0"));
  }
}
