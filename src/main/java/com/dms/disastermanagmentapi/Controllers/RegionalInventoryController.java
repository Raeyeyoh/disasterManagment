package com.dms.disastermanagmentapi.Controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dms.disastermanagmentapi.Repositories.InventoryTransferRequestRepository;
import com.dms.disastermanagmentapi.Repositories.RegionalInventoryRepository;
import com.dms.disastermanagmentapi.Repositories.UserRepository;
import com.dms.disastermanagmentapi.entities.InventoryTransferRequest;
import com.dms.disastermanagmentapi.entities.User;
import com.dms.disastermanagmentapi.enums.RequestStatus;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/regional-inventory")
public class RegionalInventoryController {

    private final InventoryTransferRequestRepository requestRepository;
    private final RegionalInventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    public RegionalInventoryController(
            InventoryTransferRequestRepository requestRepository, 
            RegionalInventoryRepository inventoryRepository, 
            UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.inventoryRepository = inventoryRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/stock")
    @PreAuthorize("hasAnyAuthority('ROLE_REGIONAL_ADMIN' ,'ROLE_REGIONAL_STAFF')")
    public ResponseEntity<?> getMyStock(Authentication auth) {
        System.out.println(auth.getAuthorities());
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        return ResponseEntity.ok(inventoryRepository.findById_RegionIdAndQuantityGreaterThan(
    user.getRegion().getRegionId(), 0
)
);
    }

@PostMapping("/request/submit")
@PreAuthorize("hasAuthority('ROLE_REGIONAL_ADMIN')")
public ResponseEntity<?> createRequest(@RequestBody InventoryTransferRequest request, Authentication auth) {
    User admin = userRepository.findByUsername(auth.getName()).orElseThrow();

    if (!request.getItemType().getAllowedUnits().contains(request.getUnit())) {
        return ResponseEntity.badRequest().body("Invalid unit for item type " + request.getItemType());
    }

    request.setRequestedBy(admin);
    request.setRegion(admin.getRegion());
    request.setStatus(RequestStatus.PENDING);
    return ResponseEntity.ok(requestRepository.save(request));
}



    @GetMapping("/requests")
@PreAuthorize("hasAuthority('ROLE_REGIONAL_ADMIN')")

    public ResponseEntity<List<InventoryTransferRequest>> getRequests(Authentication auth) {
        System.out.println(auth.getAuthorities());

        String roles = auth.getAuthorities().toString();
        if (roles.contains("ROLE_SUPER_ADMIN")) {
            return ResponseEntity.ok(requestRepository.findAll());
        } else {
            User user = userRepository.findByUsername(auth.getName()).orElseThrow();
            return ResponseEntity.ok(requestRepository.findByRegion_RegionId(user.getRegion().getRegionId()));
        }
    }

  
}