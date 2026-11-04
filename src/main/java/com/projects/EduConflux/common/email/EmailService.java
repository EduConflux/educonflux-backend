package com.projects.EduConflux.common.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${brevo.api.key:${BREVO_API_KEY:}}")
    private String brevoApiKey;

    @Value("${brevo.sender.email:${BREVO_SENDER_EMAIL:${MAIL_USERNAME:}}}")
    private String brevoSenderEmail;

    @Value("${brevo.sender.name:${BREVO_SENDER_NAME:EduConflux}}")
    private String brevoSenderName;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Async
    public void sendCredentials(
            String personalEmail,
            String institutionalEmail,
            String temporaryPassword) {

        String subject = "EduConflux Account Created";
        String content = "Welcome to EduConflux!\n\n" +
                "Your institution has created your EduConflux account.\n\n" +
                "Institutional Email: " + institutionalEmail + "\n" +
                "Temporary Password: " + temporaryPassword + "\n\n" +
                "Please log in using these credentials and change your password on your first login.\n\n" +
                "Regards,\n" +
                "EduConflux";

        sendEmail(personalEmail, null, subject, content);
    }

    @Async
    public void sendClassroomInvitation(
            String personalEmail,
            String studentName,
            String classroomName,
            String facultyName,
            String invitationCode,
            String expiresAt) {

        String subject = "Invitation to join " + classroomName;
        String content = "Hello " + studentName + ",\n\n" +
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
                "EduConflux";

        sendEmail(personalEmail, studentName, subject, content);
    }

    private void sendEmail(String toEmail, String toName, String subject, String textContent) {
        if (brevoApiKey != null && !brevoApiKey.trim().isBlank()) {
            try {
                sendViaBrevo(toEmail, toName, subject, textContent);
                return;
            } catch (Exception e) {
                log.error("Failed to send email via Brevo HTTP API to {}: {}", toEmail, e.getMessage(), e);
            }
        }

        // Fallback to JavaMail SMTP (for local dev or if Brevo is not configured)
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(textContent);
            mailSender.send(message);
            log.info("Email sent via SMTP to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email via SMTP to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    private void sendViaBrevo(String toEmail, String toName, String subject, String textContent) throws Exception {
        String sName = (brevoSenderName != null && !brevoSenderName.trim().isBlank()) ? brevoSenderName.trim() : "EduConflux";
        String sEmail = (brevoSenderEmail != null && !brevoSenderEmail.trim().isBlank()) ? brevoSenderEmail.trim() : "saiamirtheshxoxo@gmail.com";

        StringBuilder json = new StringBuilder();
        json.append("{")
                .append("\"sender\":{\"name\":\"").append(escapeJson(sName)).append("\",\"email\":\"").append(escapeJson(sEmail)).append("\"},")
                .append("\"to\":[{\"email\":\"").append(escapeJson(toEmail.trim())).append("\"");

        if (toName != null && !toName.trim().isBlank()) {
            json.append(",\"name\":\"").append(escapeJson(toName.trim())).append("\"");
        }

        json.append("}],")
                .append("\"subject\":\"").append(escapeJson(subject)).append("\",")
                .append("\"textContent\":\"").append(escapeJson(textContent)).append("\"")
                .append("}");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("accept", "application/json")
                .header("api-key", brevoApiKey.trim())
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString(), StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            log.info("Email successfully sent via Brevo HTTP API to {} (status: {})", toEmail, response.statusCode());
        } else {
            log.error("Brevo API returned error status {} for recipient {}: {}", response.statusCode(), toEmail, response.body());
            throw new RuntimeException("Brevo API error: " + response.body());
        }
    }

    private String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (ch <= '\u001F') {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
                }
            }
        }
        return sb.toString();
    }
}
