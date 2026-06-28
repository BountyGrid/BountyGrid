package com.bountygrid.controller;

import com.bountygrid.dto.LoginRequest;
import com.bountygrid.dto.RegisterRequest;
import com.bountygrid.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest("", ""));
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginRequest request, BindingResult bindingResult,
                        HttpServletResponse response, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) return "login";
        addJwtCookie(response, authService.login(request.email(), request.password()));
        redirectAttributes.addFlashAttribute("success", "Welcome back");
        return "redirect:/home";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest("", "", "", "", ""));
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest request, BindingResult bindingResult,
                           HttpServletResponse response, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) return "register";
        addJwtCookie(response, authService.register(request));
        redirectAttributes.addFlashAttribute("success", "Account created");
        return "redirect:/home";
    }

    private void addJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);
    }
}
