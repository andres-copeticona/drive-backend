package com.drive.drive.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JWTInterceptor implements HandlerInterceptor {

  private final JwtUtil jwtUtil;

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

  private void sendError(HttpServletResponse response, int status, String message) throws IOException {
    response.setStatus(status);
    response.setContentType("application/json");
    response.getWriter().write("{\"error\":\"" + message + "\"}");
    response.getWriter().flush();
  }
}
