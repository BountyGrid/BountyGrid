package com.bountygrid.controller;

import com.bountygrid.entity.User;
import com.bountygrid.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

@RequiredArgsConstructor
abstract class ControllerSupport {
    private final UserService userService;

    protected User currentUser(Authentication authentication) {
        return userService.getByEmail(authentication.getName());
    }
}
