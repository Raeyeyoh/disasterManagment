package com.dms.disastermanagmentapi.Controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dms.disastermanagmentapi.Repositories.AidDistributionRepository;
import com.dms.disastermanagmentapi.Repositories.IncidentReportRepository;
import com.dms.disastermanagmentapi.Repositories.NotificationRepository;
import com.dms.disastermanagmentapi.Repositories.RegionalInventoryRepository;
import com.dms.disastermanagmentapi.Repositories.UserRepository;
import com.dms.disastermanagmentapi.Repositories.VictimRepository;
import com.dms.disastermanagmentapi.dto.AidRequest;
import com.dms.disastermanagmentapi.entities.AidDistribution;
import com.dms.disastermanagmentapi.entities.IncidentReport;
import com.dms.disastermanagmentapi.entities.Notification;
import com.dms.disastermanagmentapi.entities.RegionalInventory;
import com.dms.disastermanagmentapi.entities.User;
import com.dms.disastermanagmentapi.entities.Victim;
import com.dms.disastermanagmentapi.enums.NotificationType;

import jakarta.transaction.Transactional;
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/distribution")
public class DistributionController {

    private final VictimRepository victimRepository;
    private final AidDistributionRepository distributionRepository;
    private final RegionalInventoryRepository inventoryRepository;
    private final IncidentReportRepository incidentRepository;
    private final UserRepository userRepository;

    private final NotificationRepository notificationRepository;

public DistributionController(VictimRepository victimRepository, 
                              AidDistributionRepository distributionRepository,
                              RegionalInventoryRepository inventoryRepository,
                              IncidentReportRepository incidentRepository,
                              UserRepository userRepository,
                              NotificationRepository notificationRepository) { 
    this.victimRepository = victimRepository;
    this.distributionRepository = distributionRepository;
    this.inventoryRepository = inventoryRepository;
    this.incidentRepository = incidentRepository;
    this.userRepository = userRepository;
    this.notificationRepository = notificationRepository; 
}
@GetMapping("/my-region-stock")
public ResponseEntity<?> getMyRegionStock(Authentication auth) {
    User staff = userRepository.findByUsername(auth.getName()).orElseThrow();
    return ResponseEntity.ok(inventoryRepository.findById_RegionId(staff.getRegion().getRegionId())
            .stream()
            .filter(item -> item.getQuantity() > 0)
            .collect(Collectors.toList()));
}
//hasAuthority('ROLE_REGIONAL_STAFF'), or("hasRole('VOLUNTEER')")
   // @PreAuthorize("hasRole('ROLE_VOLUNTEER') or hasAuthority('RESOURCE_ALLOCATE')")
//

    @PostMapping("/give-aid")
    @PreAuthorize("hasAuthority('RESOURCE_ALLOCATE')")
@Transactional
public ResponseEntity<?> distributeAid(@RequestBody AidRequest dto, Authentication auth) {
    System.out.println("User Authorities: " + auth.getAuthorities());
    User staff = userRepository.findByUsername(auth.getName()).orElseThrow();
    IncidentReport incident = incidentRepository.findById(dto.getIncidentId())
            .orElseThrow(() -> new RuntimeException("Incident not found"));
    
    Victim victim = victimRepository.findByNationalId(dto.getNationalId())
            .orElseGet(() -> {
                Victim newVictim = new Victim();
                newVictim.setFullName(dto.getVictimName());
                newVictim.setNationalId(dto.getNationalId());
                newVictim.setRegion(staff.getRegion());
                return victimRepository.save(newVictim);
            });

   
    RegionalInventory stock = inventoryRepository
            .findByRegion_RegionIdAndItemName(staff.getRegion().getRegionId(), dto.getItemName())
            .orElseThrow(() -> new RuntimeException("Stock not found for: " + dto.getItemName()));

    if (stock.getQuantity() < dto.getQuantity()) {
        return ResponseEntity.badRequest().body("Insufficient stock! Available: " + stock.getQuantity());
    }

    stock.setQuantity(stock.getQuantity() - dto.getQuantity());
    stock.setLastUpdated(java.time.Instant.now());
    inventoryRepository.save(stock); 

    int LOW_STOCK_THRESHOLD = 10; 
    if (stock.getQuantity() <= LOW_STOCK_THRESHOLD) {
        List<User> admins = userRepository.findByRegionAndRoleNames(staff.getRegion(), List.of("ROLE_REGIONAL_ADMIN"));
        for (User admin : admins) {
            Notification alert = new Notification();
            alert.setUser(admin);
            alert.setType(NotificationType.LOW_STOCK);
            alert.setMessage("URGENT: " + stock.getItemName() + " is low (" + stock.getQuantity() + " left).");
            notificationRepository.save(alert);
        }
    }

    AidDistribution distribution = new AidDistribution();
    distribution.setVictim(victim);
    distribution.setIncident(incident);
    distribution.setItemName(dto.getItemName());
    distribution.setQuantityGiven(dto.getQuantity());
    distribution.setDistributedBy(staff);
    distribution.setDistributedAt(java.time.Instant.now());
    distributionRepository.save(distribution);

return ResponseEntity.ok("Successfully distributed " + dto.getQuantity() + 
                         " " + dto.getItemName() + " to " + victim.getFullName());}
                         @GetMapping("/analytics/summary")
@PreAuthorize("hasAnyAuthority('ROLE_REGIONAL_ADMIN','ROLE_SUPER_ADMIN')")
public ResponseEntity<?> getAidDistributionSummary() {
    return ResponseEntity.ok(
        distributionRepository.getTotalAidByItem()
    );
}



}
