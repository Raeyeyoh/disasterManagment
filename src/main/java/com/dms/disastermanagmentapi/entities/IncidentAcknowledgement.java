package com.dms.disastermanagmentapi.entities;


import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "incident_acknowledgements")
@Data
public class IncidentAcknowledgement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "incident_id", nullable = false)
    private IncidentReport incident;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String role; 

    @Enumerated(EnumType.STRING)
    private AckStatus status;

    private Instant acknowledgedAt;

    public enum AckStatus {
        VIEWED, RESPONDING, RESOLVED
        //
    }
}