package com.dms.disastermanagmentapi.Services;


import com.dms.disastermanagmentapi.Repositories.UserRepository;
import com.dms.disastermanagmentapi.entities.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        Set<GrantedAuthority> authorities = user.getUserRoles().stream()
    .flatMap(userRole -> {
        var role = userRole.getRole();
        var auths = Stream.of(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));
        
        var perms = role.getRolePermissionMappings().stream()
            .map(m -> new SimpleGrantedAuthority(m.getPermission().getPermissionName()));
        
        return Stream.concat(auths, perms);
    })
    .collect(Collectors.toSet());

return new org.springframework.security.core.userdetails.User(
        user.getUsername(), 
        user.getPasswordHash(),
        user.getStatus().name().equalsIgnoreCase("APPROVED"), 
        true, true, true, 
        authorities
);
    }
}
