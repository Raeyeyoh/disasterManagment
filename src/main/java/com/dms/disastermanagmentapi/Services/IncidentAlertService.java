package com.dms.disastermanagmentapi.Services;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dms.disastermanagmentapi.Repositories.IncidentReportRepository;
import com.dms.disastermanagmentapi.Repositories.NotificationRepository;
import com.dms.disastermanagmentapi.Repositories.UserRepository;
import com.dms.disastermanagmentapi.entities.IncidentReport;
import com.dms.disastermanagmentapi.entities.Notification;
import com.dms.disastermanagmentapi.entities.User;
import com.dms.disastermanagmentapi.enums.IncidentStatus;
import com.dms.disastermanagmentapi.enums.NotificationType;
import com.dms.disastermanagmentapi.enums.SeverityLevel;

@Service
public class IncidentAlertService {

    @Autowired
    private IncidentReportRepository incidentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;
    @Autowired

        UserRepository userRepository;


    @Scheduled(fixedRate = 300000) 
public void notifyUnresolvedIncidents() {
    Instant now = Instant.now();
    List<IncidentReport> pendingIncidents = incidentRepository.findByStatus(IncidentStatus.PENDING);

    for (IncidentReport inc : pendingIncidents) {
        if (inc.getSeverity() != SeverityLevel.CRITICAL && inc.getSeverity() != SeverityLevel.HIGH) {
            continue; 
        }

        long minutesToWait = (inc.getSeverity() == SeverityLevel.CRITICAL) ? 15 : 60;
        Duration threshold = Duration.ofMinutes(minutesToWait);

        Instant lastNotified = inc.getLastNotifiedAt();
        
        if (lastNotified != null && lastNotified.plus(threshold).isAfter(now)) {
            continue;
        }

        List<User> admins = userRepository.findByRegionAndRoleNames(inc.getRegion(), List.of("ROLE_REGIONAL_ADMIN"));
        
        for (User admin : admins) {
            sendAlert(admin, inc, now);
        }

        inc.setLastNotifiedAt(now);
        incidentRepository.save(inc);
    }
}

private void sendAlert(User user, IncidentReport inc, Instant now) {
    Notification note = new Notification();
    note.setUser(user);
    note.setType(NotificationType.URGENT);
    note.setMessage("🚨 URGENT [" + inc.getSeverity() + "]: " + inc.getTitle() + " is still PENDING!");
    note.setReferenceId(inc.getReportId());
    note.setCreatedAt(now);
    notificationRepository.save(note);

    emailService.sendUrgentAlert(user.getContact(), "High Severity Alert", note.getMessage());
}
}

