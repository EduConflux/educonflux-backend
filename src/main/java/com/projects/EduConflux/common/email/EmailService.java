package com.projects.EduConflux.common.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendCredentials(
            String personalEmail,
            String institutionalEmail,
            String temporaryPassword) {

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
    }

    public void sendClassroomInvitation(
            String personalEmail,
            String studentName,
            String classroomName,
            String facultyName,
            String invitationLink,
            String expiresAt) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(personalEmail);

        message.setSubject(
                "Invitation to join " + classroomName
        );

        message.setText(
                "Hello " + studentName + ",\n\n" +

                        facultyName +
                        " has invited you to join the EduConflux classroom:\n\n" +

                        "Classroom: " + classroomName + "\n\n" +

                        "Join the classroom using this invitation link:\n" +
                        invitationLink + "\n\n" +

                        "This invitation expires on:\n" +
                        expiresAt + "\n\n" +

                        "Log in using your EduConflux account after opening the link.\n\n" +

                        "Regards,\n" +
                        "EduConflux"
        );

        mailSender.send(message);
    }
}