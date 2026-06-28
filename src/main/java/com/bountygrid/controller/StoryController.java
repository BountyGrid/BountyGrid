package com.bountygrid.controller;

import com.bountygrid.dto.StoryRequest;
import com.bountygrid.service.StoryService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StoryController extends ControllerSupport {
    private final StoryService storyService;

    public StoryController(UserService userService, StoryService storyService) {
        super(userService);
        this.storyService = storyService;
    }

    @GetMapping("/stories")
    public String stories(Model model) {
        model.addAttribute("stories", storyService.getPublicStories());
        model.addAttribute("storyRequest", new StoryRequest(null, "", "", null, true));
        return "stories/index";
    }

    @PostMapping("/stories/create")
    public String create(@Valid @ModelAttribute StoryRequest request, BindingResult bindingResult,
                         Authentication authentication, RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            storyService.create(currentUser(authentication), request);
            redirectAttributes.addFlashAttribute("success", "Story published");
        }
        return "redirect:/stories";
    }

    @PostMapping("/stories/{id}/react")
    public String react(@PathVariable Long id, @RequestParam(defaultValue = "heart") String reaction) {
        storyService.react(id, reaction);
        return "redirect:/stories";
    }
}
