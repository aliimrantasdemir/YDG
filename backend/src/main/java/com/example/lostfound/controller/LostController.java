package com.example.lostfound.controller;

import com.example.lostfound.model.User;
import com.example.lostfound.repo.UserRepository;
import com.example.lostfound.service.LostService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LostController {
  private final UserRepository users;
  private final LostService lost;

  public LostController(UserRepository users, LostService lost) {
    this.users = users;
    this.lost = lost;
  }

  @GetMapping("/lost/new")
  public String newForm() { return "lost_new"; }

  @PostMapping("/lost/new")
  public String create(Authentication auth,
                       @RequestParam String title,
                       @RequestParam String description,
                       @RequestParam String category,
                       @RequestParam String location,
                       @RequestParam String dateLost,
                       Model model) {
    User u = users.findByEmail(auth.getName()).orElseThrow();
    try {
      lost.create(u.id(), title, description, category, location, dateLost);
      return "redirect:/lost/my";
    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      return "lost_new";
    }
  }

  @GetMapping("/lost/my")
  public String my(Authentication auth, Model model) {
    User u = users.findByEmail(auth.getName()).orElseThrow();
    model.addAttribute("items", lost.myLost(u.id()));
    return "lost_my";
  }
}
