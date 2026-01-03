package com.example.lostfound.controller;

import com.example.lostfound.model.User;
import com.example.lostfound.repo.UserRepository;
import com.example.lostfound.service.ClaimService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ClaimController {
  private final UserRepository users;
  private final ClaimService claims;

  public ClaimController(UserRepository users, ClaimService claims) {
    this.users = users;
    this.claims = claims;
  }

  @GetMapping("/claim/new/{foundId}")
  public String form(@PathVariable long foundId, Model model) {
    model.addAttribute("foundId", foundId);
    return "claim_new";
  }

  @PostMapping("/claim/new")
  public String create(Authentication auth,
                       @RequestParam long foundId,
                       @RequestParam String proofText,
                       Model model) {
    User u = users.findByEmail(auth.getName()).orElseThrow();
    try {
      claims.createClaim(u.id(), foundId, proofText);
      return "redirect:/found/list";
    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      model.addAttribute("foundId", foundId);
      return "claim_new";
    }
  }
}
