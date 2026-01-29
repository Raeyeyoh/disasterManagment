package com.dms.disastermanagmentapi.entities;


import com.dms.disastermanagmentapi.enums.ItemType;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "regionalinventory")
@Data
@NoArgsConstructor
public class RegionalInventory {

    @EmbeddedId
    private RegionalInventoryId id;
    
@ManyToOne(fetch = FetchType.EAGER)
@MapsId("regionId")
    @JoinColumn(name = "region_id", insertable = false, updatable = false)
    private Region region;

    @Column(name = "item_name", length = 100, nullable = false)
    private String itemName; 
    
    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType type;

    @Column(length = 20)
    private String unit; 

    @Column(name = "last_updated")
    private java.time.Instant lastUpdated = java.time.Instant.now();
}