package com.example.lostfound.config;

import com.example.lostfound.model.Role;
import com.example.lostfound.repo.UserRepository;
import com.example.lostfound.util.TimeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SeedDataConfig {

  @Bean
  CommandLineRunner seedUsers(@Value("${app.seed.enabled:true}") boolean enabled,
                              UserRepository users,
                              PasswordEncoder encoder) {
    return args -> {
      if (!enabled) return;
      seed(users, encoder, "Admin", "admin@demo.com", "admin123", Role.ADMIN);
      seed(users, encoder, "Staff", "staff@demo.com", "staff123", Role.STAFF);
      seed(users, encoder, "User",  "user@demo.com",  "user123",  Role.USER);
    };
  }

  private void seed(UserRepository users, PasswordEncoder encoder, String name, String email, String pass, Role role) {
    if (users.findByEmail(email).isPresent()) return;
    users.insert(name, email, encoder.encode(pass), role, TimeUtil.nowIso());
  }
}
