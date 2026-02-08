package com.ecutrans9000.backend.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserPrincipalService userPrincipalService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7);
    Claims claims;
    try {
      claims = jwtService.parseClaims(token);
    } catch (Exception ex) {
      filterChain.doFilter(request, response);
      return;
    }

    String username = claims.getSubject();
    if (SecurityContextHolder.getContext().getAuthentication() != null) {
      filterChain.doFilter(request, response);
      return;
    }

    UserDetails userDetails;
    Object uidClaim = claims.get("uid");
    if (uidClaim != null) {
      try {
        userDetails = userPrincipalService.loadUserById(UUID.fromString(uidClaim.toString()));
      } catch (Exception ex) {
        filterChain.doFilter(request, response);
        return;
      }
    } else if (username != null) {
      userDetails = userPrincipalService.loadUserByUsername(username);
    } else {
      filterChain.doFilter(request, response);
      return;
    }

    boolean tokenValid = uidClaim != null
        ? jwtService.isTokenNotExpired(token)
        : jwtService.isTokenValid(token, userDetails.getUsername());
    if (!tokenValid) {
      filterChain.doFilter(request, response);
      return;
    }

    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails.getAuthorities());
    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authToken);
    filterChain.doFilter(request, response);
  }
}
