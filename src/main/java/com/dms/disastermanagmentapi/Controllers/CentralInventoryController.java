package com.dms.disastermanagmentapi.Controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dms.disastermanagmentapi.Repositories.CentralInventoryRepository;
import com.dms.disastermanagmentapi.Repositories.InventoryTransferRequestRepository;
import com.dms.disastermanagmentapi.Repositories.UserRepository;
import com.dms.disastermanagmentapi.Services.InventoryPriorityService;
import com.dms.disastermanagmentapi.Services.InventoryService;
import com.dms.disastermanagmentapi.entities.CentralInventory;
import com.dms.disastermanagmentapi.entities.InventoryTransferRequest;
import com.dms.disastermanagmentapi.entities.User;
import com.dms.disastermanagmentapi.enums.PriorityLevel;
import com.dms.disastermanagmentapi.enums.RequestStatus;
@CrossOrigin(origins = "http://127.0.0.1:5500") 
@RestController
@RequestMapping("/api/central-inventory")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class CentralInventoryController {

    @Autowired
    private CentralInventoryRepository centralRepo;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryTransferRequestRepository transferRepo;

    @GetMapping("/stock")
    public List<CentralInventory> getCentralStock() {
        return centralRepo.findAll();
    }

    @PostMapping("/stock/add")
    public ResponseEntity<?> addStock(@RequestBody CentralInventory item) {
        return centralRepo.findByItemName(item.getItemName())
            .map(existing -> {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                centralRepo.save(existing);
                return ResponseEntity.ok("Central stock updated.");
            })
            .orElseGet(() -> {
                centralRepo.save(item);
                return ResponseEntity.ok("New resource added to Central.");
            });
    }

    @Autowired
private InventoryPriorityService priorityService;

@GetMapping("/requests/pending")
public List<InventoryTransferRequest> getPendingRequests() {

    List<InventoryTransferRequest> requests =
        transferRepo.findByStatus(RequestStatus.PENDING);
requests.forEach(r -> {
    PriorityLevel priority = priorityService.calculatePriority(
        r.getRegion().getRiskLevel(),
         r.getItemType(),
        r.getQuantity(),
        r.getCreatedAt()
    );
    r.setPriority(priority);
});
requests.sort((a, b) -> {
    PriorityLevel p1 = priorityService.calculatePriority(
        a.getRegion().getRiskLevel(),
       a.getItemType(), 
        a.getQuantity(),
        a.getCreatedAt()
    );

    PriorityLevel p2 = priorityService.calculatePriority(
        b.getRegion().getRiskLevel(),
        b.getItemType(),
        b.getQuantity(),
        b.getCreatedAt()
    );

    return p2.compareTo(p1);
});



return requests;
}


   @PutMapping("/requests/{id}/approve")
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
public ResponseEntity<?> approveRequest(@PathVariable Integer id, Authentication auth) {
    System.out.println("User Authorities: " + auth.getAuthorities());

    try {
        String username = auth.getName();
        
        User admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        
        inventoryService.approveTransfer(id, admin);
        
        return ResponseEntity.ok("Transfer successful and record allocated.");
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
@PostMapping("/requests/{id}/reject")
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")

public ResponseEntity<?> rejectRequest(@PathVariable Integer id) {
    InventoryTransferRequest request = transferRepo.findById(id)
        .orElseThrow(() -> new RuntimeException("Request not found"));

    request.setStatus(RequestStatus.REJECTED); 
    transferRepo.save(request);

    return ResponseEntity.ok().build();
}
@GetMapping("/analytics/approved-by-region")
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
public ResponseEntity<?> getApprovedRequestsByRegion() {

    List<Object[]> results =
            transferRepo.totalQuantityApprovedRequestsByRegion();

    List<Map<String, Object>> response = results.stream()
           .map(row -> {
    Map<String, Object> map = new HashMap<>();
    map.put("region", row[0]);
    map.put("itemType", row[1]);
    map.put("totalQuantity", row[2]);
    return map;
})

            .toList();

    return ResponseEntity.ok(response);
}

}
