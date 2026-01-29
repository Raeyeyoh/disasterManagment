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
@Table(name = "aid_distributions")
@Data
@NoArgsConstructor
public class AidDistribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer distributionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "victim_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
    private Victim victim;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "incident_id")
    private IncidentReport incident;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Integer quantityGiven;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "distributed_by")
    private User distributedBy;

    private java.time.Instant distributedAt = java.time.Instant.now();
}
