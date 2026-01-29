package com.dms.disastermanagmentapi.entities;

import java.util.List;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")

    private Integer roleId; 

    @Column(name = "role_name", length = 50, nullable = false, unique = true)
    private String roleName; 

@Column(columnDefinition = "TEXT")
private String description;
    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER)
private List<RolePermissionMapping> rolePermissionMappings;
}