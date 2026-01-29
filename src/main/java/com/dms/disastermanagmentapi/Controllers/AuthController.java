package com.dms.disastermanagmentapi.Controllers;


import com.dms.disastermanagmentapi.Repositories.UserRepository;
import com.dms.disastermanagmentapi.Services.AuthService;
import com.dms.disastermanagmentapi.dto.*;
import com.dms.disastermanagmentapi.entities.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
private final PasswordEncoder passwordEncoder;
    public AuthController(AuthService authService, UserRepository userRepository,PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Map<String, Object> response = authService.loginUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Error: " + e.getMessage());
        }
    }
@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody RegisterRequest reg) {
    try {
        authService.registerUser(reg);
        return ResponseEntity.ok("User registered successfully.");
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
    @PostMapping("/change-password")
public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request, Authentication auth) {
    String currentPassword = request.get("currentPassword");
    String newPassword = request.get("newPassword");

    User user = userRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
        return ResponseEntity.badRequest().body("Incorrect current password");
    }

    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    return ResponseEntity.ok("Password updated successfully");
}
}