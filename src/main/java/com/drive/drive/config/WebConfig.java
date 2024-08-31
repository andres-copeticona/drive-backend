package com.drive.drive.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.drive.drive.security.JWTInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final JWTInterceptor jwtTokenInterceptor;

  @Autowired
  public WebConfig(JWTInterceptor jwtTokenInterceptor) {
    this.jwtTokenInterceptor = jwtTokenInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(jwtTokenInterceptor);
  }
}
