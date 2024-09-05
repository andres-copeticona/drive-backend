package com.drive.drive.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JWTInterceptor implements HandlerInterceptor {

  private Logger log = LoggerFactory.getLogger(JWTInterceptor.class);

  private final JwtUtil jwtUtil;

  private final List<String> staticPublicPaths = Arrays.asList("/api/doc", "/api/v3/api-docs");

  public JWTInterceptor(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    if (request.getRequestURI().contains("/error")) {
      return true;
    }

    if (!(handler instanceof HandlerMethod)) {
      return true;
    }

    HandlerMethod handlerMethod = (HandlerMethod) handler;
    if (handlerMethod.hasMethodAnnotation(IsPublic.class)) {
      return true;
    }

    log.info("Request URI: {}", request.getRequestURI());

    if (isSwaggerPath(request.getRequestURI())) {
      return true;
    }

    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String jwtToken = authHeader.substring(7);
      try {

        if (jwtUtil.validateToken(jwtToken)) {
          String username = jwtUtil.extractUsername(jwtToken);
          request.setAttribute("username", username);
          return true;
        } else {
          sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado");
          return false;
        }
      } catch (Exception e) {
        sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
        return false;
      }
    } else {
      sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token no proporcionado");
      return false;
    }
  }

  private boolean isSwaggerPath(String path) {
    return staticPublicPaths.stream().anyMatch(path::startsWith);
  }

  private void sendError(HttpServletResponse response, int status, String message) throws IOException {
    response.setStatus(status);
    response.setContentType("application/json");
    response.getWriter().write("{\"error\":\"" + message + "\"}");
    response.getWriter().flush();
  }
}
