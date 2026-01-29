package com.dms.disastermanagmentapi.Security;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    private String jwtSecret = "mySecretKeyForDisasterManagementApiWhichIsVeryLongAndSecure";
    private int jwtExpirationMs = 86400000; 

   private Key key() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes());
}
    public String generateJwtToken(Authentication authentication) {
    UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

    List<String> roles = userPrincipal.getAuthorities().stream()
            .map(item -> item.getAuthority())
            .collect(Collectors.toList());

    return Jwts.builder()
            .setSubject(userPrincipal.getUsername())
            .claim("roles", roles) 
            .setIssuedAt(new Date())
            .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
            .signWith(key(), SignatureAlgorithm.HS256)
            .compact();
}

public String getUserNameFromJwtToken(String token) {
    return Jwts.parserBuilder()
               .setSigningKey(key())
               .build()
               .parseClaimsJws(token)
               .getBody()
               .getSubject();
}

public boolean validateJwtToken(String authToken) {
    try {
        Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
        return true;
    } catch (Exception e) {
        System.out.println("Invalid JWT: " + e.getMessage());
    }
    return false;
}

public List<SimpleGrantedAuthority> getAuthoritiesFromJwtToken(String token) {
    Claims claims = Jwts.parserBuilder()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(token)
            .getBody();

    List<String> roles = (List<String>) claims.get("roles");
    
    return roles.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
}
}
