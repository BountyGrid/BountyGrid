package com.bountygrid.controller;

import com.bountygrid.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController extends ControllerSupport {
    public ProfileController(UserService userService) {
        super(userService);
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        model.addAttribute("user", currentUser(authentication));
        return "profile";
    }
}
