package com.bountygrid.controller;

import com.bountygrid.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final AlertService alertService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/home")
    public String home(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "10") Double radius,
            Model model) {
        if (lat != null && lng != null) {
            model.addAttribute("alerts", alertService.findNearby(lat, lng, radius));
        } else {
            model.addAttribute("alerts", alertService.getActiveAlerts());
        }
        return "home";
    }
}
