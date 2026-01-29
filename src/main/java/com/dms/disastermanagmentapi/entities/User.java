package com.dms.disastermanagmentapi.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

import com.dms.disastermanagmentapi.enums.UserStatus;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;
    @Column(length = 50, unique = true, nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Column(length = 100, nullable = false)
    private String contact;

    private String location;

    @Enumerated(EnumType.STRING) 
    @Column(nullable = false )
    private UserStatus status=UserStatus.PENDING;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "region_id") 
    private Region region;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
   
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<UserRole> userRoles;

   

    
}
