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
}