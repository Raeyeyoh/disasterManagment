package com.dms.disastermanagmentapi.entities;


import jakarta.persistence.*;
import lombok.Data;
//import lombok.NoArgsConstructor;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "userroles")
@Data
public class UserRole {

    @EmbeddedId
    private UserRoleId id = new UserRoleId(); 

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @MapsId("roleId")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt = Instant.now();
    
    public UserRole() {}

    public UserRole(User user, Role role) {
        this.user = user;
        this.role = role;
        this.assignedAt = Instant.now();
    }
}