package com.ecutrans9000.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.MessageDigest;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final Key key;
  private final long expirationMinutes;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expiration-minutes:120}") long expirationMinutes) {
    this.key = buildKey(secret);
    this.expirationMinutes = expirationMinutes;
  }

  public String generateToken(String subject, Map<String, Object> claims) {
    Instant now = Instant.now();
    Instant expiresAt = now.plusSeconds(expirationMinutes * 60);
    return Jwts.builder()
        .subject(subject)
        .claims(claims)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiresAt))
        .signWith(key)
        .compact();
  }

  public Claims parseClaims(String token) {
    return Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build().parseSignedClaims(token).getPayload();
  }

  public boolean isTokenValid(String token, String expectedSubject) {
    Claims claims = parseClaims(token);
    return expectedSubject != null
        && expectedSubject.equalsIgnoreCase(claims.getSubject())
        && claims.getExpiration().after(new Date());
  }

  public boolean isTokenNotExpired(String token) {
    Claims claims = parseClaims(token);
    return claims.getExpiration().after(new Date());
  }

  private Key buildKey(String secret) {
    try {
      byte[] keyBytes = MessageDigest.getInstance("SHA-256").digest(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      return Keys.hmacShaKeyFor(keyBytes);
    } catch (Exception ex) {
      throw new IllegalStateException("No se pudo inicializar la clave JWT", ex);
    }
  }
}
