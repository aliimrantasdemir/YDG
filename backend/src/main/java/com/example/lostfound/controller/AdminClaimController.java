package com.example.lostfound.controller;

import com.example.lostfound.model.User;
import com.example.lostfound.repo.UserRepository;
import com.example.lostfound.service.ClaimService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminClaimController {
  private final UserRepository users;
  private final ClaimService claims;

  public AdminClaimController(UserRepository users, ClaimService claims) {
    this.users = users;
    this.claims = claims;
  }

  @GetMapping("/admin/claims")
  public String list(Model model) {
    model.addAttribute("items", claims.listPending());
    return "admin_claims";
  }

  @PostMapping("/admin/claims/{id}/approve")
  public String approve(Authentication auth, @PathVariable long id) {
    User admin = users.findByEmail(auth.getName()).orElseThrow();
    claims.approve(admin.id(), id);
    return "redirect:/admin/claims";
  }

  @PostMapping("/admin/claims/{id}/reject")
  public String reject(Authentication auth, @PathVariable long id) {
    User admin = users.findByEmail(auth.getName()).orElseThrow();
    claims.reject(admin.id(), id);
    return "redirect:/admin/claims";
  }
}
