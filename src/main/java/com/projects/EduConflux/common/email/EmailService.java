package com.projects.EduConflux.common.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendCredentials(
            String personalEmail,
            String institutionalEmail,
            String temporaryPassword) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(personalEmail);
            message.setSubject("EduConflux Account Created");

            message.setText(
                    "Welcome to EduConflux!\n\n" +
                            "Your institution has created your EduConflux account.\n\n" +
                            "Institutional Email: " + institutionalEmail + "\n" +
                            "Temporary Password: " + temporaryPassword + "\n\n" +
                            "Please log in using these credentials and change your password on your first login.\n\n" +
                            "Regards,\n" +
                            "EduConflux"
            );

            mailSender.send(message);
            log.info("Credentials email sent successfully to {}", personalEmail);
        } catch (Exception e) {
            log.error("Failed to send credentials email to {}: {}", personalEmail, e.getMessage(), e);
        }
    }

    @Async
    public void sendClassroomInvitation(
            String personalEmail,
            String studentName,
            String classroomName,
            String facultyName,
            String invitationCode,
            String expiresAt) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(personalEmail);

            message.setSubject(
                    "Invitation to join " + classroomName
            );

            message.setText(
                    "Hello " + studentName + ",\n\n" +
                            facultyName +
                            " has invited you to join the EduConflux classroom:\n\n" +
                            "Classroom: " + classroomName + "\n" +
                            "Invitation Code: " + invitationCode + "\n\n" +
                            "This invitation expires on:\n" +
                            expiresAt + "\n\n" +
                            "To join the classroom:\n" +
                            "1. Log in to your EduConflux student account.\n" +
                            "2. Navigate to Classrooms > Join Classroom.\n" +
                            "3. Enter the invitation code: " + invitationCode + "\n\n" +
                            "Regards,\n" +
                            "EduConflux"
            );

            mailSender.send(message);
            log.info("Classroom invitation email sent successfully to {}", personalEmail);
        } catch (Exception e) {
            log.error("Failed to send classroom invitation email to {}: {}", personalEmail, e.getMessage(), e);
        }
    }
}
