package com.dms.disastermanagmentapi.entities;


import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true) 
public class RolePermissionId extends CompositeKeyBase {

    private Integer roleId;
    private Integer permissionId; 
}