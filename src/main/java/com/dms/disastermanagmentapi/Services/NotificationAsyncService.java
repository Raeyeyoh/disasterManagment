package com.dms.disastermanagmentapi.Services;
import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.dms.disastermanagmentapi.Repositories.NotificationRepository;
import com.dms.disastermanagmentapi.Repositories.UserRepository;
import com.dms.disastermanagmentapi.entities.IncidentReport;
import com.dms.disastermanagmentapi.entities.Notification;
import com.dms.disastermanagmentapi.entities.User;
import com.dms.disastermanagmentapi.enums.NotificationType;
import com.dms.disastermanagmentapi.enums.UserStatus;

@Service
public class NotificationAsyncService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public NotificationAsyncService(
            UserRepository userRepository,
            NotificationRepository notificationRepository,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    @Async
    public void notifyResponders(IncidentReport savedReport, User staff) {

        try {
            List<String> responderRoles =
                    List.of("POLICE", "VOLUNTEER", "REGIONAL_ADMIN","REGIONAL_STAFF");

            List<User> responders =
                    userRepository.findByRegionAndRoleNames(

                            staff.getRegion(), responderRoles);
                            
                           List<User> filteredResponders = responders.stream()
    .filter(u -> !u.getUserId().equals(staff.getUserId()))
    .filter(u -> u.getStatus().equals(UserStatus.APPROVED)) 
    .toList();


            for (User responder : filteredResponders) {

                Notification note = new Notification();
                note.setUser(responder);
                note.setMessage("⚠️ " + savedReport.getSeverity()
                        + " INCIDENT: " + savedReport.getTitle());
                note.setIsRead(false);
                note.setType(NotificationType.INCIDENT_ASSIGNED);
                note.setCreatedAt(Instant.now());
                note.setReferenceId(savedReport.getReportId());
                

                notificationRepository.save(note);

                boolean isVolunteer = responder.getUserRoles().stream()
                        .anyMatch(ur ->
                                ur.getRole().getRoleName()
                                        .equals("ROLE_VOLUNTEER"));

                if (!isVolunteer) {
                    emailService.sendUrgentAlert(
                            responder.getContact(),
                            "DMS Alert",
                            "New incident: " + savedReport.getTitle()
                    );
                }
            }

        } catch (Exception e) {
            System.err.println("Async notification failed: " + e.getMessage());
        }
    }
}
