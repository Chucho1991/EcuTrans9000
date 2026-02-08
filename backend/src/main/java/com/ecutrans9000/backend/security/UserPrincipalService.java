package com.ecutrans9000.backend.security;

import com.ecutrans9000.backend.adapters.out.persistence.entity.UserJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.repository.UserJpaRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPrincipalService implements UserDetailsService {

  private final UserJpaRepository userJpaRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserJpaEntity user = userJpaRepository.findByUsernameIgnoreCase(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    return new AuthUserPrincipal(user);
  }

  public UserDetails loadUserById(UUID id) {
    UserJpaEntity user = userJpaRepository.findById(id)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    return new AuthUserPrincipal(user);
  }
}
