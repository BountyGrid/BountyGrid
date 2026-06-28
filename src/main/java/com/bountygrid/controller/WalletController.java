package com.bountygrid.controller;

import com.bountygrid.entity.User;
import com.bountygrid.service.UserService;
import com.bountygrid.service.WalletService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WalletController extends ControllerSupport {
    private final WalletService walletService;

    public WalletController(UserService userService, WalletService walletService) {
        super(userService);
        this.walletService = walletService;
    }

    @GetMapping("/wallet")
    public String wallet(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        model.addAttribute("user", user);
        model.addAttribute("transactions", walletService.getTransactions(user));
        return "wallet";
    }

    @PostMapping("/wallet/deposit")
    public String deposit(@RequestParam double amount, Authentication authentication, RedirectAttributes redirectAttributes) {
        walletService.deposit(currentUser(authentication), amount);
        redirectAttributes.addFlashAttribute("success", "Deposit added");
        return "redirect:/wallet";
    }

    @PostMapping("/wallet/withdraw")
    public String withdraw(@RequestParam double amount, Authentication authentication, RedirectAttributes redirectAttributes) {
        walletService.withdraw(currentUser(authentication), amount);
        redirectAttributes.addFlashAttribute("success", "Withdrawal recorded");
        return "redirect:/wallet";
    }
}
