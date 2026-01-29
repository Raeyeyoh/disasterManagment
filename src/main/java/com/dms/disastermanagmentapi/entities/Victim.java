package com.dms.disastermanagmentapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "victims")
@Data
@NoArgsConstructor
public class Victim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer victimId;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true)
    private String nationalId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
    private Region region;

    private java.time.Instant registeredAt = java.time.Instant.now();
}
