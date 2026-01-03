package com.example.lostfound.controller;

import com.example.lostfound.service.FoundService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FoundController {
  private final FoundService found;
  public FoundController(FoundService found) { this.found = found; }

  @GetMapping("/found/list")
  public String list(@RequestParam(required = false) String category,
                     @RequestParam(required = false) String location,
                     Model model) {
    model.addAttribute("items", found.list(category, location));
    model.addAttribute("category", category == null ? "" : category);
    model.addAttribute("location", location == null ? "" : location);
    return "found_list";
  }
}
