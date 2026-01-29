package com.dms.disastermanagmentapi.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "role_permission_mapping")
@Data
@NoArgsConstructor
public class RolePermissionMapping {

    @EmbeddedId 
    private RolePermissionId id;

    @MapsId("roleId") 
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    @JsonIgnore
    private Role role;

    @MapsId("permissionId") 
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "permission_id")
    private Permission permission;
}