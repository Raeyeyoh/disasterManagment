package com.dms.disastermanagmentapi.entities;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "regions")
@Data 
@NoArgsConstructor 
public class Region {
        @Id
    @Column(name = "region_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer regionId; 

    @Column(name = "region_name", length = 100, nullable = false)
    private String regionName; 
 
    @Column(name = "risk_level")
    private Integer riskLevel;
}