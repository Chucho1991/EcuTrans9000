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

/**
 * Componente publico de backend para ApiAuditFilter.
 */
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
      String requestBody = truncate(extractBody(requestWrapper.getContentAsByteArray(), requestWrapper.getContentType()));
      String responseBody = truncate(extractBody(responseWrapper.getContentAsByteArray(), responseWrapper.getContentType()));
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String username = authentication != null ? authentication.getName() : "ANON";
      String role = authentication != null && authentication.getAuthorities() != null
          ? authentication.getAuthorities().stream().findFirst().map(a -> a.getAuthority()).orElse("ANON")
          : "ANON";
      auditService.saveApiAudit(endpoint, requestBody, responseBody, username, role);
      responseWrapper.copyBodyToResponse();
    }
  }

  private String extractBody(byte[] bytes, String contentType) {
    if (bytes == null || bytes.length == 0) {
      return "";
    }
    if (isBinaryContentType(contentType) || hasNullByte(bytes)) {
      return "[BINARY CONTENT OMITTED] size=" + bytes.length + " bytes";
    }
    return new String(bytes, StandardCharsets.UTF_8);
  }

  private boolean isBinaryContentType(String contentType) {
    if (contentType == null || contentType.isBlank()) {
      return false;
    }
    String ct = contentType.toLowerCase();
    return ct.startsWith("image/")
        || ct.contains("application/pdf")
        || ct.contains("application/octet-stream")
        || ct.contains("multipart/form-data");
  }

  private boolean hasNullByte(byte[] bytes) {
    for (byte b : bytes) {
      if (b == 0x00) {
        return true;
      }
    }
    return false;
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
