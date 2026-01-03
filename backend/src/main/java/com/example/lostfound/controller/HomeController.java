package com.example.lostfound.controller;

import com.example.lostfound.model.User;
import com.example.lostfound.repo.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
  private final UserRepository users;
  public HomeController(UserRepository users) { this.users = users; }

  @GetMapping("/")
  public String home(Authentication auth, Model model) {
    User u = users.findByEmail(auth.getName()).orElseThrow();
    model.addAttribute("user", u);
    model.addAttribute("role", u.role().name());
    return "home";
  }
}
