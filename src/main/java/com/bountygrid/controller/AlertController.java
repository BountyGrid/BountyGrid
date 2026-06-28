package com.bountygrid.controller;

import com.bountygrid.dto.AlertRequest;
import com.bountygrid.dto.TipRequest;
import com.bountygrid.entity.Alert.AlertCategory;
import com.bountygrid.entity.Alert.AlertType;
import com.bountygrid.service.AlertService;
import com.bountygrid.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/alerts")
public class AlertController extends ControllerSupport {
    private final AlertService alertService;

    public AlertController(UserService userService, AlertService alertService) {
        super(userService);
        this.alertService = alertService;
    }

    @GetMapping("/post")
    public String postForm(Model model) {
        model.addAttribute("alertRequest", new AlertRequest("", "", AlertType.LOST, AlertCategory.OTHER, null, null, "", 5.0, 0.0));
        model.addAttribute("types", AlertType.values());
        model.addAttribute("categories", AlertCategory.values());
        return "alerts/post";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute AlertRequest request, BindingResult bindingResult,
                         @RequestParam(required = false) MultipartFile photo,
                         Authentication authentication, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("types", AlertType.values());
            model.addAttribute("categories", AlertCategory.values());
            return "alerts/post";
        }
        alertService.create(currentUser(authentication), request, photo);
        redirectAttributes.addFlashAttribute("success", "Alert posted");
        return "redirect:/home";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("alert", alertService.getById(id));
        model.addAttribute("tipRequest", new TipRequest(""));
        return "alerts/detail";
    }

    @PostMapping("/{id}/tips")
    public String tip(@PathVariable Long id, @Valid @ModelAttribute TipRequest request,
                      Authentication authentication, RedirectAttributes redirectAttributes) {
        alertService.submitTip(currentUser(authentication), id, request.content());
        redirectAttributes.addFlashAttribute("success", "Tip submitted");
        return "redirect:/alerts/" + id;
    }
}
