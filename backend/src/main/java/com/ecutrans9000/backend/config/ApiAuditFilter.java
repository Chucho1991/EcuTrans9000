package com.ecutrans9000.backend.config;

import com.ecutrans9000.backend.service.AuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
@RequiredArgsConstructor
public class ApiAuditFilter extends OncePerRequestFilter {

  private final AuditService auditService;

  @Value("${app.audit.max-payload-size:10000}")
  private int maxPayloadSize;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
    ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
    try {
      filterChain.doFilter(requestWrapper, responseWrapper);
    } finally {
      String endpoint = request.getMethod() + " " + request.getRequestURI();
      String requestBody = truncate(extractBody(requestWrapper.getContentAsByteArray()));
      String responseBody = truncate(extractBody(responseWrapper.getContentAsByteArray()));
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String username = authentication != null ? authentication.getName() : "ANON";
      String role = authentication != null && authentication.getAuthorities() != null
          ? authentication.getAuthorities().stream().findFirst().map(a -> a.getAuthority()).orElse("ANON")
          : "ANON";
      auditService.saveApiAudit(endpoint, requestBody, responseBody, username, role);
      responseWrapper.copyBodyToResponse();
    }
  }

  private String extractBody(byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      return "";
    }
    return new String(bytes, StandardCharsets.UTF_8);
  }

  private String truncate(String content) {
    if (content == null) {
      return null;
    }
    if (content.length() <= maxPayloadSize) {
      return content;
    }
    return content.substring(0, maxPayloadSize) + "...[TRUNCATED]";
  }
}
