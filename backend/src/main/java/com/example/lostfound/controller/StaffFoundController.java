package com.example.lostfound.controller;

import com.example.lostfound.model.User;
import com.example.lostfound.repo.UserRepository;
import com.example.lostfound.service.FoundService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class StaffFoundController {
  private final UserRepository users;
  private final FoundService found;

  public StaffFoundController(UserRepository users, FoundService found) {
    this.users = users;
    this.found = found;
  }

  @GetMapping("/found/new")
  public String newForm() { return "found_new"; }

  @PostMapping("/found/new")
  public String create(Authentication auth,
                       @RequestParam String title,
                       @RequestParam String description,
                       @RequestParam String category,
                       @RequestParam String location,
                       @RequestParam String dateFound,
                       @RequestParam String shelfCode,
                       Model model) {
    User u = users.findByEmail(auth.getName()).orElseThrow();
    try {
      found.createWithCustody(u.id(), title, description, category, location, dateFound, shelfCode);
      return "redirect:/found/list";
    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      return "found_new";
    }
  }
}
