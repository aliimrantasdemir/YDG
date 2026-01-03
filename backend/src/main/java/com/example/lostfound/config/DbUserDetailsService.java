package com.example.lostfound.config;

import com.example.lostfound.model.User;
import com.example.lostfound.repo.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DbUserDetailsService implements UserDetailsService {
  private final UserRepository users;
  public DbUserDetailsService(UserRepository users) { this.users = users; }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User u = users.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("not_found"));
    return org.springframework.security.core.userdetails.User
        .withUsername(u.email())
        .password(u.passwordHash())
        .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + u.role().name())))
        .build();
  }
}
