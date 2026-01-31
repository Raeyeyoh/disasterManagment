package com.dms.disastermanagmentapi.Controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dms.disastermanagmentapi.Repositories.UserRepository;
import com.dms.disastermanagmentapi.Services.AuthService;
import com.dms.disastermanagmentapi.entities.Role;
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
@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_REGIONAL_ADMIN')")
public ResponseEntity<?> getPendingUsers() {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();
    User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));

    Role currentRole = currentUser.getUserRoles().get(0).getRole();
    String currentRoleName = currentRole.getRoleName();

    List<User> pendingUsers = userRepository.findByStatus(UserStatus.PENDING)
            .stream()
            .filter(u -> {
                Role targetRole = u.getUserRoles().get(0).getRole();
                String targetRoleName = targetRole.getRoleName();

                if (currentRoleName.equals("SUPER_ADMIN")) {
                    return targetRoleName.equals("REGIONAL_ADMIN") || targetRoleName.equals("REGIONAL_STAFF");
                } else if (currentRoleName.equals("REGIONAL_ADMIN")) {
                    return (targetRoleName.equals("POLICE") || targetRoleName.equals("VOLUNTEER")) &&
                            u.getRegion().equals(currentUser.getRegion());
                } else {
                    return false;
                }
            }).toList();

    System.out.println("DEBUG: Found " + pendingUsers.size() + " pending users for role: " + currentRoleName);
    return ResponseEntity.ok(pendingUsers);
}


    
    @PutMapping("/approve/{id}") 
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_REGIONAL_ADMIN')")
    public ResponseEntity<?> approveUser(@PathVariable Integer id) {
        authService.approveAndActivateUser(id); 
        return ResponseEntity.ok("User approved successfully!");
    }
@GetMapping("/pending-debug")
public ResponseEntity<?> pendingDebug(Authentication auth) {
    System.out.println("Logged in user: " + auth.getName());
    System.out.println("Authorities: " + auth.getAuthorities());
    return ResponseEntity.ok("Check logs");
}

     
}
