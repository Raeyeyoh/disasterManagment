package com.dms.disastermanagmentapi.entities;


import com.dms.disastermanagmentapi.enums.ItemType;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "centralinventory")
@Data
@NoArgsConstructor
public class CentralInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer item_Id;

    @Column(name = "item_name", length = 100, nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType type;

    @Column(length = 20)
    private String unit;
}