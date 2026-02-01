package com.dms.disastermanagmentapi.Controllers;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dms.disastermanagmentapi.Repositories.UserRepository;
import com.dms.disastermanagmentapi.Services.NotificationAsyncService;
import com.dms.disastermanagmentapi.Services.SeverityPredictionService;
import com.dms.disastermanagmentapi.Repositories.AcknowledgementRepository;
import com.dms.disastermanagmentapi.Repositories.FeedBackRepository;
import com.dms.disastermanagmentapi.Repositories.IncidentReportRepository;
import com.dms.disastermanagmentapi.entities.FeedBack;
import com.dms.disastermanagmentapi.entities.IncidentAcknowledgement;
import com.dms.disastermanagmentapi.entities.IncidentReport;
import com.dms.disastermanagmentapi.entities.Region;
import com.dms.disastermanagmentapi.entities.User;
import com.dms.disastermanagmentapi.enums.IncidentStatus;
import com.dms.disastermanagmentapi.enums.SeverityLevel;
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentReportRepository incidentRepository;
    private final UserRepository userRepository;
    private final FeedBackRepository feedbackRepository; 
    private final NotificationAsyncService notificationAsyncService;
    private final AcknowledgementRepository acknowledgementRepository;
    

    public IncidentController(
        IncidentReportRepository incidentRepository, 
                              UserRepository userRepository, 
                              FeedBackRepository feedbackRepository,
                                NotificationAsyncService notificationAsyncService,
                                AcknowledgementRepository acknowledgementRepository
                                
                            ) {
                                
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
        this.feedbackRepository = feedbackRepository;
        this.notificationAsyncService = notificationAsyncService;
        this.acknowledgementRepository = acknowledgementRepository;
   
    }
    @Autowired
private SeverityPredictionService severityPredictionService;
@PostMapping("/report")
@PreAuthorize("hasAuthority('ROLE_REGIONAL_STAFF')")
public ResponseEntity<?> createReport(@RequestBody IncidentReport incidentData, Authentication authentication) {
    
    User staff = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
            Region staffRegion = staff.getRegion(); 
incidentData.setRegion(staffRegion);
      String  predictedSeverityStr = "MEDIUM"; 
      
try {
    predictedSeverityStr = severityPredictionService.predictSeverity(
        incidentData.getTitle().name(),
        incidentData.getDescription().length(),
        staffRegion.getRegionName(),
       staffRegion.getRiskLevel()
    );
} catch (Exception e) {
    e.printStackTrace();
}


SeverityLevel predictedSeverity;
try {
    predictedSeverity = SeverityLevel.valueOf(predictedSeverityStr.toUpperCase());
} catch (IllegalArgumentException e) {
    e.printStackTrace();
    predictedSeverity = SeverityLevel.MEDIUM; 
}
System.out.println("RAW PYTHON OUTPUT: " + predictedSeverityStr);

incidentData.setSeverity(predictedSeverity);

    incidentData.setStatus(IncidentStatus.PENDING);
incidentData.setCreatedAt(java.time.Instant.now());
incidentData.setReportedBy(staff);

IncidentReport savedReport = incidentRepository.save(incidentData);

notificationAsyncService.notifyResponders(savedReport, staff);

return ResponseEntity.ok(Map.of(
        "status", "success",
        "message", "Incident reported successfully!",
        "incidentId", savedReport.getReportId()
));
  }


    @GetMapping
    @PreAuthorize("hasAnyAuthority( 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<List<IncidentReport>> getAllIncidents() {
        return ResponseEntity.ok(incidentRepository.findAll());
    }

  @GetMapping("/my-region")
@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_POLICE', 'ROLE_REGIONAL_STAFF', 'ROLE_REGIONAL_ADMIN', 'ROLE_VOLUNTEER')")
public ResponseEntity<List<IncidentReport>> getIncidentsByRegion(Authentication authentication) {
    String username = authentication.getName();
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

    boolean isSuperAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

    if (isSuperAdmin) {
        return ResponseEntity.ok(incidentRepository.findAll());
    }

    if (user.getRegion() == null) {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
Integer regionId = user.getRegion().getRegionId();
    
   
List<IncidentReport> pendingReports = incidentRepository.findByRegion_RegionIdAndStatus(
    regionId, 
    IncidentStatus.PENDING 
);

return ResponseEntity.ok(pendingReports);
}

@GetMapping("/test-auth")
public Authentication test(Authentication auth) {
    return auth;
}



@GetMapping("/{id}")
@PreAuthorize("hasAuthority('ROLE_REGIONAL_STAFF')")
public IncidentReport getIncident(@PathVariable Integer id) {
  
    return incidentRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Incident not found"));
}

@PostMapping("/acknowledge/{incidentId}")
public ResponseEntity<?> acknowledgeIncident(
    @PathVariable Integer incidentId, 
   @RequestBody Map<String, String> body,
    Authentication auth) {
        String statusStr = body.get("status");
    IncidentAcknowledgement.AckStatus status = IncidentAcknowledgement.AckStatus.valueOf(statusStr);

    User responder = userRepository.findByUsername(auth.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));

    IncidentReport incident = incidentRepository.findById(incidentId)
        .orElseThrow(() -> new RuntimeException("Incident not found"));

    IncidentAcknowledgement ack = acknowledgementRepository
        .findByIncidentAndUser(incident, responder)
        .orElse(new IncidentAcknowledgement());

    ack.setIncident(incident);
    ack.setUser(responder);
    ack.setStatus(status);
    ack.setAcknowledgedAt(Instant.now());
    
    ack.setRole(responder.getUserRoles().iterator().next().getRole().getRoleName());

    acknowledgementRepository.save(ack);

    return ResponseEntity.ok("Status updated to " + status);
}

@GetMapping("/incident/{incidentId}")
@PreAuthorize("hasAnyAuthority('ROLE_REGIONAL_ADMIN', 'ROLE_SUPER_ADMIN','ROLE_POLICE', 'ROLE_REGIONAL_STAFF', 'ROLE_VOLUNTEER')")
public List<IncidentAcknowledgement> getIncidentResponders(@PathVariable Integer incidentId) {
    return acknowledgementRepository.findByIncident_ReportId(incidentId);
}

@GetMapping("/all-active")
@PreAuthorize("hasAuthority('ROLE_REGIONAL_ADMIN')")
public List<IncidentAcknowledgement> getAllAcknowledgements() {
    return acknowledgementRepository.findAllByOrderByAcknowledgedAtDesc();
}
@GetMapping("/regional-activity")
@PreAuthorize("hasAuthority('ROLE_REGIONAL_ADMIN')")
public ResponseEntity<List<IncidentAcknowledgement>> getRegionalActivity(Authentication authentication) {
    User admin = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("Admin not found"));

    if (admin.getRegion() == null) {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }

  List<IncidentAcknowledgement> activity = acknowledgementRepository
    .findByIncident_Region_RegionIdAndIncident_StatusOrderByAcknowledgedAtDesc(
        admin.getRegion().getRegionId(), 
        IncidentStatus.PENDING
    );

    return ResponseEntity.ok(activity);
}

@Transactional 
@PostMapping("/{id}/feedback")
public ResponseEntity<?> submitFeedback(@PathVariable Integer id, @RequestBody FeedBack feedbackDto, Authentication auth) {
    IncidentReport report = incidentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Incident not found"));
    
    User currentUser = userRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

    String userRole = auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .filter(role -> role.startsWith("ROLE_")) 
        .map(role -> role.replace("ROLE_", ""))   
        .findFirst()
        .orElse("");

    String timeStamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    
    String newLine = String.format("\n[%s] %s (%s): %s", 
            timeStamp, 
            currentUser.getName(), 
            userRole, 
            feedbackDto.getMessage());

    FeedBack existingLog = feedbackRepository.findByReport_ReportId(id)
            .orElse(new FeedBack());

    if (existingLog.getFeedbackId() == null) {
        existingLog.setReport(report);
        existingLog.setRegion(report.getRegion());
        existingLog.setMessage("--- Incident FeedBack Started ---" + newLine);
    } else {
        existingLog.setMessage(existingLog.getMessage() + newLine);
    }

    existingLog.setSubmittedBy(currentUser);
    existingLog.setCreatedAt(java.time.Instant.now());
    feedbackRepository.save(existingLog);
    if ("REGIONAL_ADMIN".equals(userRole)) {
    report.setStatus(IncidentStatus.RESOLVED);
    incidentRepository.save(report);
}

    if (!userRole.equals("REGIONAL_ADMIN")) {
        IncidentAcknowledgement ack = acknowledgementRepository
                .findByIncidentAndUser(report, currentUser)
                .orElse(new IncidentAcknowledgement());

        ack.setIncident(report);
        ack.setUser(currentUser);
        ack.setStatus(IncidentAcknowledgement.AckStatus.RESOLVED);
        ack.setAcknowledgedAt(java.time.Instant.now());
        ack.setRole("ROLE_" + userRole); 

        acknowledgementRepository.save(ack);
        return ResponseEntity.ok(Map.of("message", "Status updated to RESOLVED and feedback added."));
    }

    return ResponseEntity.ok(Map.of("message", "Update added to the incident history."));
}


@GetMapping("/feedback/my-region")
@PreAuthorize("hasAnyAuthority('ROLE_REGIONAL_ADMIN')")
public ResponseEntity<List<FeedBack>> getRegionalFeedback(Authentication auth) {
    User user = userRepository.findByUsername(auth.getName()).orElseThrow();
    
    List<FeedBack> logs = feedbackRepository.findByRegion_RegionIdOrderByCreatedAtDesc(
        user.getRegion().getRegionId()
    );
    
    return ResponseEntity.ok(logs);
}
@GetMapping("/{id}/feedback")
@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_REGIONAL_ADMIN', 'ROLE_POLICE', 'ROLE_VOLUNTEER', 'ROLE_REGIONAL_STAFF')")
public ResponseEntity<?> getIncidentFeedback(@PathVariable Integer id) {
    return feedbackRepository.findByReport_ReportId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.ok(new FeedBack())); 
}
@Transactional

@PostMapping("/{id}/resolve")
@PreAuthorize("hasRole('REGIONAL_ADMIN')")
public ResponseEntity<?> resolveIncident(@PathVariable Integer id, Authentication auth) {
    IncidentReport report = incidentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Incident not found"));
    
    report.setStatus(IncidentStatus.RESOLVED);
    incidentRepository.save(report);

    User currentUser = userRepository.findByUsername(auth.getName()).orElseThrow();
    FeedBack existingLog = feedbackRepository.findByReport_ReportId(id).orElse(new FeedBack());

    String timeStamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    String resolutionNote = String.format("\n[%s] *** SYSTEM: Status changed to RESOLVED by %s ***", timeStamp, currentUser.getName());
String currentMessage = existingLog.getMessage() != null ? existingLog.getMessage() : "--- Incident Log Started ---";
    existingLog.setMessage(currentMessage + resolutionNote);
    existingLog.setSubmittedBy(currentUser);
    existingLog.setCreatedAt(java.time.Instant.now());
    
    feedbackRepository.save(existingLog);

    return ResponseEntity.ok("Incident marked as RESOLVED.");
}

}

