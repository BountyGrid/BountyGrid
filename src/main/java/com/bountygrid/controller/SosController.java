package com.bountygrid.controller;

import com.bountygrid.service.SosService;
import com.bountygrid.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SosController extends ControllerSupport {
    private final SosService sosService;

    public SosController(UserService userService, SosService sosService) {
        super(userService);
        this.sosService = sosService;
    }

    @GetMapping("/sos")
    public String sos(Model model) {
        model.addAttribute("broadcasts", sosService.getActive());
        return "sos";
    }

    @PostMapping("/sos/broadcast")
    public String broadcast(@RequestParam Long alertId, @RequestParam(defaultValue = "25") double radiusKm,
                            Authentication authentication, RedirectAttributes redirectAttributes) {
        sosService.broadcast(currentUser(authentication), alertId, radiusKm);
        redirectAttributes.addFlashAttribute("success", "SOS broadcast sent");
        return "redirect:/sos";
    }
}
