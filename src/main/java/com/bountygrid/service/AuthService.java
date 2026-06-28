package com.bountygrid.service;

import com.bountygrid.dto.RegisterRequest;
import com.bountygrid.entity.User;
import com.bountygrid.repository.UserRepository;
import com.bountygrid.security.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .city(request.city())
                .password(passwordEncoder.encode(request.password()))
                .build();
        userRepository.save(user);
        return jwtUtils.generateToken(user.getEmail());
    }

    public String login(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        return jwtUtils.generateToken(email);
    }

    public String refreshToken(String email) {
        return jwtUtils.generateToken(email);
    }
}
