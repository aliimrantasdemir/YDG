package com.example.lostfound.controller;

import com.example.lostfound.model.User;
import com.example.lostfound.repo.UserRepository;
import com.example.lostfound.service.HandoverService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class StaffHandoverController {
  private final UserRepository users;
  private final HandoverService handover;

  public StaffHandoverController(UserRepository users, HandoverService handover) {
    this.users = users;
    this.handover = handover;
  }

  @GetMapping("/staff/handovers")
  public String list(Model model) {
    model.addAttribute("items", handover.listNotDelivered());
    return "staff_handovers";
  }

  @PostMapping("/staff/handovers/deliver")
  public String deliver(Authentication auth,
                        @RequestParam long claimId,
                        @RequestParam String otp,
                        @RequestParam String receiverName) {
    User staff = users.findByEmail(auth.getName()).orElseThrow();
    handover.deliver(staff.id(), claimId, otp, receiverName);
    return "redirect:/staff/handovers";
  }
}
