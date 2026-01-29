package com.dms.disastermanagmentapi.dto;

public record RegisterRequest(String username, 
    String password, 
    String name, 
    String contact, 
    String location,
    String roleName,
    Integer regionId
    ) {

   

}
