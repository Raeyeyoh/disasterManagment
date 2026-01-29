package com.dms.disastermanagmentapi.entities;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

import com.dms.disastermanagmentapi.enums.ItemType;
import com.dms.disastermanagmentapi.enums.PriorityLevel;
import com.dms.disastermanagmentapi.enums.RequestStatus;

@Entity
@Table(name = "inventorytransferrequests")
@Data
@NoArgsConstructor
public class InventoryTransferRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer requestId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "region_id")
    private Region region;

    @Column(name = "item_name", length = 100, nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Integer quantity;
    @Transient
private PriorityLevel priority;


    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requested_by")
    private User requestedBy; 

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
private ItemType itemType;

}
