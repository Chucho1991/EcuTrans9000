package com.ecutrans9000.backend.security;

import com.ecutrans9000.backend.adapters.out.persistence.entity.UserJpaEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class AuthUserPrincipal implements UserDetails {

  private final UUID id;
  private final String username;
  private final String password;
  private final boolean enabled;
  private final Collection<? extends GrantedAuthority> authorities;

  public AuthUserPrincipal(UserJpaEntity user) {
    this.id = user.getId();
    this.username = user.getUsername();
    this.password = user.getPasswordHash();
    this.enabled = Boolean.TRUE.equals(user.getActivo()) && !Boolean.TRUE.equals(user.getDeleted());
    this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRol().name()));
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
}
