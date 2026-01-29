package com.dms.disastermanagmentapi.entities;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

import com.dms.disastermanagmentapi.enums.IncidentStatus;
import com.dms.disastermanagmentapi.enums.IncidentTitle;
import com.dms.disastermanagmentapi.enums.SeverityLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "incidentreports")
@Data
@NoArgsConstructor
public class IncidentReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")

    private Integer reportId;

@ManyToOne(fetch = FetchType.EAGER)  
  @JoinColumn(name = "region_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
    private Region region;

 @Enumerated(EnumType.STRING)
    @Column(name = "title", length = 200, nullable = false)
    private IncidentTitle title;

@Column(columnDefinition = "TEXT")
private String description;
@Column(name = "location")
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeverityLevel severity;

    @ManyToOne(fetch = FetchType.EAGER)  
    @JoinColumn(name = "reported_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
    private User reportedBy;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    
    @Column(name = "last_notified_at")
    private Instant lastNotifiedAt;

    public Instant getLastNotifiedAt() {
        return lastNotifiedAt;
    }

    public void setLastNotifiedAt(Instant lastNotifiedAt) {
        this.lastNotifiedAt = lastNotifiedAt;
    }
    @Transient
    public Duration getAlertInterval() {
        switch (this.severity) {
            case HIGH: return Duration.ofMinutes(1);
            case MEDIUM: return Duration.ofHours(1);
            case LOW: return Duration.ofHours(2);
            default: return Duration.ofHours(1);
        }}

}
