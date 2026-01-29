package com.dms.disastermanagmentapi.entities;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")

    private Integer permissionId;

    @Column(name = "permission_name", length = 50, nullable = false, unique = true)
    private String permissionName;

@Column(columnDefinition = "TEXT")
private String description;
}
