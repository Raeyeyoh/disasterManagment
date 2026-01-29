package com.dms.disastermanagmentapi.Services;

import com.dms.disastermanagmentapi.Repositories.*;
import com.dms.disastermanagmentapi.dto.*;
import com.dms.disastermanagmentapi.entities.*;
import com.dms.disastermanagmentapi.enums.UserStatus;
import com.dms.disastermanagmentapi.Security.JwtUtils; 
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegionRepository regionRepository; 
    private final JwtUtils jwtUtils; 
    private final EmailService emailService;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
                       RoleRepository roleRepository, UserRoleRepository userRoleRepository,
                       RegionRepository regionRepository,
                       PasswordEncoder passwordEncoder, JwtUtils jwtUtils,EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.regionRepository = regionRepository;
        this.jwtUtils = jwtUtils;
        this.emailService = emailService;
    }

    public Map<String, Object> loginUser(LoginRequest loginRequest) {
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
    );

    String jwt = jwtUtils.generateJwtToken(authentication);
    User user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
   
String role = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .filter(auth -> auth.startsWith("ROLE_")) 
        .findFirst()
        .orElse("USER");
System.out.println("JWT Authority being set: " + role);
    Map<String, Object> response = new HashMap<>();
    response.put("token", jwt);
    response.put("username", authentication.getName());
    response.put("role", role); 
    response.put("userId", user.getUserId());
    return response;
}

   @Transactional 
    public void registerUser(RegisterRequest reg) {
        if (userRepository.findByUsername(reg.username()).isPresent()) {
            throw new RuntimeException("Error: Username is already taken!");
        }
if ("ROLE_REGIONAL_ADMIN".equalsIgnoreCase(reg.roleName())) {
        if (userRepository.countRegionalAdmins(reg.regionId()) > 0) {
            throw new RuntimeException("Error: Region already has a Regional Admin assigned!");
        }
    }
        User user = new User();
        user.setUsername(reg.username());
        user.setPasswordHash(passwordEncoder.encode(reg.password()));
        user.setName(reg.name());
        user.setContact(reg.contact());
        user.setLocation(reg.location());
        user.setStatus(UserStatus.PENDING);

        if (reg.regionId() != null) {
            Region region = regionRepository.findById(reg.regionId())
                .orElseThrow(() -> new RuntimeException("Region not found"));
            user.setRegion(region);
        }

        User savedUser = userRepository.save(user);

        System.out.println("Looking for role: [" + reg.roleName() + "]");
        Role role = roleRepository.findByRoleName(reg.roleName())
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));

        UserRole userRole = new UserRole();
        userRole.getId().setUserId(savedUser.getUserId());
        userRole.getId().setRoleId(role.getRoleId());
        
        userRole.setUser(savedUser);
        userRole.setRole(role);
        userRole.setAssignedAt(java.time.Instant.now());

        userRoleRepository.save(userRole);
    }
@Transactional
    public void approveAndActivateUser(Integer userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(UserStatus.APPROVED); 
        userRepository.save(user);

        try {
            emailService.sendApprovalEmail(user.getContact(), user.getName());
            System.out.println("Email successfully sent to: " + user.getContact());
        } catch (Exception e) {
            System.err.println("Database updated, but email failed: " + e.getMessage());
        }
        
        System.out.println("User " + user.getUsername() + " is now active.");
    }
}