package com.drive.drive.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.drive.drive.security.AccessUserArgumentResolver;
import com.drive.drive.security.JWTInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final JWTInterceptor jwtTokenInterceptor;

  private final AccessUserArgumentResolver currentUserArgumentResolver;

  public WebConfig(AccessUserArgumentResolver currentUserArgumentResolver, JWTInterceptor jwtTokenInterceptor) {
    this.currentUserArgumentResolver = currentUserArgumentResolver;
    this.jwtTokenInterceptor = jwtTokenInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(jwtTokenInterceptor);
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(currentUserArgumentResolver);
  }
}
