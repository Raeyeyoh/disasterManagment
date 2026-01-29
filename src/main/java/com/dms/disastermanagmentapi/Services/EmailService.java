package com.dms.disastermanagmentapi.Services;


import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Service 
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendApprovalEmail(String toEmail, String name) {
        SimpleMailMessage message = new SimpleMailMessage(); 
        message.setFrom("raeyeyohannes@gmail.com"); 
        message.setTo(toEmail);
        message.setSubject("Account Approved - Disaster Management System");
        message.setText("Hello " + name + ",\n\nYour account has been approved by the admin. You can now log in to the system.\n\nRegards,\nThe Team");

        mailSender.send(message);
    }

   public void sendUrgentAlert(String toEmail, String subject, String body) {
    if (toEmail == null || !toEmail.contains("@")) {
        System.err.println("Skipping email: '" + toEmail + "' is not a valid email address.");
        return; 
    }

    try {
        SimpleMailMessage message = new SimpleMailMessage(); 
        message.setFrom("raeyeyohannes@gmail.com"); 
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        System.out.println("Alert email sent successfully to: " + toEmail);
    } catch (Exception e) {
        System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
    }
}
    
}
