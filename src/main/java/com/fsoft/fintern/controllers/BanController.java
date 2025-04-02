package com.fsoft.fintern.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/banned")
public class BanController {
    @GetMapping()
    public String showBannedPage() {
        return "banned"; // This will resolve to templates/banned.html
    }
}
