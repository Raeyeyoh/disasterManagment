package com.dms.disastermanagmentapi.entities;


import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RegionalInventoryId extends CompositeKeyBase {

    private Integer itemId;   
    private Integer regionId;  
}
