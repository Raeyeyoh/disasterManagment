package com.dms.disastermanagmentapi.dto;

import lombok.Data;

@Data
public class AidRequest {
    private String victimName;
    private String nationalId;
    private Integer incidentId;
    private String itemName;
    private Integer quantity;
}
