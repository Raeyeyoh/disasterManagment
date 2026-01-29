package com.dms.disastermanagmentapi.entities;


//import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "auditlogs")
@Data
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "action_type", length = 50, nullable = false)
    private String actionType;

    @Column(name = "table_affected", length = 50, nullable = false)
    private String tableAffected;

    @Column(name = "record_id", nullable = false)
    private Integer recordId;

    @Column(name = "old_value", columnDefinition = "jsonb") 
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "jsonb")
    private String newValue;

    @Column(name = "ip_address", length = 45, nullable = false)
    private String ipAddress;

    
    // @Column(name = "user_agent", nullable = false)
    // private String userAgent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
