package com.dms.disastermanagmentapi.Controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dms.disastermanagmentapi.Repositories.UserRepository;
import com.dms.disastermanagmentapi.Services.AuthService;
import com.dms.disastermanagmentapi.entities.User;
import com.dms.disastermanagmentapi.enums.UserStatus;
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://127.0.0.1:5500")

public class UserController {

    private final UserRepository userRepository;
private final AuthService authService; 
    public UserController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @GetMapping("/pending")
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getPendingUsers() {
        List<User> pendingUsers = userRepository.findByStatus(UserStatus.PENDING);
        System.out.println("DEBUG: Found " + pendingUsers.size() + " pending users in database.");
    
    return ResponseEntity.ok(pendingUsers);
    }

    @PutMapping("/approve/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> approveUser(@PathVariable Integer id) {
        authService.approveAndActivateUser(id); 
        return ResponseEntity.ok("User approved successfully!");
    }
}
