package com.projects.EduConflux.classroom.chat.service;

import com.projects.EduConflux.classroom.chat.dto.ChatMessageResponse;
import com.projects.EduConflux.classroom.chat.entity.ChatMessage;
import com.projects.EduConflux.classroom.chat.entity.ChatMessageType;
import com.projects.EduConflux.classroom.chat.repository.ChatMessageRepository;
import com.projects.EduConflux.classroom.entity.Classroom;
import com.projects.EduConflux.classroom.repository.ClassroomRepository;
import com.projects.EduConflux.classroom.membership.entity.ClassroomMembership;
import com.projects.EduConflux.classroom.membership.entity.MembershipStatus;
import com.projects.EduConflux.classroom.membership.repository.ClassroomMembershipRepository;
import com.projects.EduConflux.common.security.AuthenticatedUser;
import com.projects.EduConflux.faculty.entity.Faculty;
import com.projects.EduConflux.faculty.repository.FacultyRepository;
import com.projects.EduConflux.identity.entity.User;
import com.projects.EduConflux.identity.repository.UserRepository;
import com.projects.EduConflux.student.entity.Student;
import com.projects.EduConflux.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomMembershipRepository membershipRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;


    // =========================================================
    // GROUP CHAT
    // =========================================================

    @Transactional
    public ChatMessageResponse sendGroupMessage(
            Long classroomId,
            String content,
            Principal principal) {

        AuthenticatedUser authenticatedUser =
                extractAuthenticatedUser(principal);

        Long userId =
                authenticatedUser.getUserId();

        Long institutionId =
                authenticatedUser.getInstitutionId();

        String email =
                authenticatedUser.getEmail();

        List<String> roles =
                authenticatedUser.getRoles();


        // Resolve authenticated User
        User sender =
                userRepository
                        .findByIdAndInstitutionId(
                                userId,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Authenticated user not found"
                                ));


        // Find classroom inside institution
        Classroom classroom =
                classroomRepository
                        .findByIdAndInstitutionId(
                                classroomId,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Classroom not found"
                                ));


        // STUDENT
        if (roles.contains("STUDENT")) {

            Student student =
                    studentRepository
                            .findByInstitutionIdAndEmail(
                                    institutionId,
                                    email
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Student not found"
                                    ));


            ClassroomMembership membership =
                    membershipRepository
                            .findByClassroomIdAndStudentId(
                                    classroomId,
                                    student.getId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "You are not a member of this classroom"
                                    ));


            if (membership.getStatus()
                    != MembershipStatus.ACTIVE) {

                throw new RuntimeException(
                        "You are not an active member of this classroom"
                );
            }
        }


        // FACULTY
        else if (roles.contains("FACULTY")) {

            Faculty faculty =
                    facultyRepository
                            .findByInstitutionIdAndEmail(
                                    institutionId,
                                    email
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Faculty not found"
                                    ));


            if (!classroom.getFaculty()
                    .getId()
                    .equals(faculty.getId())) {

                throw new RuntimeException(
                        "You are not the faculty of this classroom"
                );
            }
        }


        // Other roles
        else {

            throw new RuntimeException(
                    "You are not allowed to send messages"
            );
        }


        // Create group message
        ChatMessage message =
                ChatMessage.builder()
                        .classroom(classroom)
                        .sender(sender)
                        .recipient(null)
                        .content(content)
                        .messageType(
                                ChatMessageType.TEXT
                        )
                        .build();


        ChatMessage savedMessage =
                chatMessageRepository.save(message);


        return ChatMessageResponse.builder()
                .id(savedMessage.getId())
                .classroomId(
                        classroom.getId()
                )
                .senderId(
                        sender.getId()
                )
                .senderName(
                        sender.getFirstName()
                                + " "
                                + sender.getLastName()
                )
                .content(
                        savedMessage.getContent()
                )
                .messageType(
                        savedMessage.getMessageType()
                )
                .createdAt(
                        savedMessage.getCreatedAt()
                )
                .build();
    }


    // =========================================================
    // GROUP CHAT HISTORY
    // =========================================================

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatHistory(
            Long classroomId,
            Principal principal) {

        AuthenticatedUser authenticatedUser =
                extractAuthenticatedUser(principal);

        Long institutionId =
                authenticatedUser.getInstitutionId();


        Classroom classroom =
                classroomRepository
                        .findByIdAndInstitutionId(
                                classroomId,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Classroom not found"
                                ));


        List<ChatMessage> messages =
                chatMessageRepository
                        .findByClassroomIdOrderByCreatedAtAsc(
                                classroom.getId()
                        );


        return messages.stream()
                .map(message ->
                        ChatMessageResponse.builder()
                                .id(message.getId())
                                .classroomId(
                                        message.getClassroom().getId()
                                )
                                .senderId(
                                        message.getSender().getId()
                                )
                                .senderName(
                                        message.getSender().getFirstName()
                                                + " "
                                                + message.getSender().getLastName()
                                )
                                .content(
                                        message.getContent()
                                )
                                .messageType(
                                        message.getMessageType()
                                )
                                .createdAt(
                                        message.getCreatedAt()
                                )
                                .build()
                )
                .toList();
    }


    // =========================================================
    // PRIVATE CHAT SEND
    // =========================================================

    @Transactional
    public ChatMessageResponse sendPrivateMessage(
            Long classroomId,
            Long recipientId,
            String content,
            Principal principal) {

        /*
         * Get authenticated user from the WebSocket Principal.
         */
        AuthenticatedUser authenticatedUser =
                extractAuthenticatedUser(principal);


        Long senderId =
                authenticatedUser.getUserId();

        Long institutionId =
                authenticatedUser.getInstitutionId();

        String email =
                authenticatedUser.getEmail();

        List<String> roles =
                authenticatedUser.getRoles();


        System.out.println("PRIVATE CHAT DEBUG");
        System.out.println("senderId = " + senderId);
        System.out.println("institutionId = " + institutionId);
        System.out.println("email = " + email);
        System.out.println("roles = " + roles);


        // -----------------------------------------------------
        // Resolve sender
        // -----------------------------------------------------

        User sender =
                userRepository
                        .findByIdAndInstitutionId(
                                senderId,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Authenticated user not found"
                                ));


        // -----------------------------------------------------
        // Resolve recipient
        // -----------------------------------------------------

        User recipient =
                userRepository
                        .findByIdAndInstitutionId(
                                recipientId,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Recipient not found"
                                ));


        // -----------------------------------------------------
        // Resolve classroom
        // -----------------------------------------------------

        Classroom classroom =
                classroomRepository
                        .findByIdAndInstitutionId(
                                classroomId,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Classroom not found"
                                ));


        // -----------------------------------------------------
        // Prevent self messaging
        // -----------------------------------------------------

        if (senderId.equals(recipientId)) {

            throw new RuntimeException(
                    "You cannot message yourself"
            );
        }


        // -----------------------------------------------------
        // Determine roles
        // -----------------------------------------------------

        boolean senderIsFaculty =
                roles.contains("FACULTY");

        boolean senderIsStudent =
                roles.contains("STUDENT");


        boolean recipientIsFaculty =
                userRepository
                        .findAllByInstitutionIdAndRoleName(
                                institutionId,
                                "FACULTY"
                        )
                        .stream()
                        .anyMatch(user ->
                                user.getId()
                                        .equals(recipientId)
                        );


        boolean recipientIsStudent =
                userRepository
                        .findAllByInstitutionIdAndRoleName(
                                institutionId,
                                "STUDENT"
                        )
                        .stream()
                        .anyMatch(user ->
                                user.getId()
                                        .equals(recipientId)
                        );


        // -----------------------------------------------------
        // Only FACULTY <-> STUDENT
        // -----------------------------------------------------

        boolean validConversation =
                (senderIsFaculty && recipientIsStudent)
                        ||
                        (senderIsStudent && recipientIsFaculty);


        if (!validConversation) {

            throw new RuntimeException(
                    "Private messaging is only allowed between faculty and students"
            );
        }


        // -----------------------------------------------------
        // Student sender must be classroom member
        // -----------------------------------------------------

        if (senderIsStudent) {

            Student student =
                    studentRepository
                            .findByInstitutionIdAndEmail(
                                    institutionId,
                                    email
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Student not found"
                                    ));


            ClassroomMembership membership =
                    membershipRepository
                            .findByClassroomIdAndStudentId(
                                    classroomId,
                                    student.getId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "You are not a member of this classroom"
                                    ));


            if (membership.getStatus()
                    != MembershipStatus.ACTIVE) {

                throw new RuntimeException(
                        "You are not an active member of this classroom"
                );
            }
        }


        // -----------------------------------------------------
        // Faculty sender must own classroom
        // -----------------------------------------------------

        if (senderIsFaculty) {

            Faculty faculty =
                    facultyRepository
                            .findByInstitutionIdAndEmail(
                                    institutionId,
                                    email
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Faculty not found"
                                    ));


            if (!classroom.getFaculty()
                    .getId()
                    .equals(faculty.getId())) {

                throw new RuntimeException(
                        "You are not the faculty of this classroom"
                );
            }
        }


        // -----------------------------------------------------
        // Recipient must be classroom member if student
        // -----------------------------------------------------

        if (recipientIsStudent) {

            Student recipientStudent =
                    studentRepository
                            .findByInstitutionIdAndEmail(
                                    institutionId,
                                    recipient.getEmail()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Recipient student not found"
                                    ));


            ClassroomMembership membership =
                    membershipRepository
                            .findByClassroomIdAndStudentId(
                                    classroomId,
                                    recipientStudent.getId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Recipient is not a member of this classroom"
                                    ));


            if (membership.getStatus()
                    != MembershipStatus.ACTIVE) {

                throw new RuntimeException(
                        "Recipient is not an active member of this classroom"
                );
            }
        }


        // -----------------------------------------------------
        // Create private message
        // -----------------------------------------------------

        ChatMessage message =
                ChatMessage.builder()
                        .classroom(classroom)
                        .sender(sender)
                        .recipient(recipient)
                        .content(content)
                        .messageType(
                                ChatMessageType.TEXT
                        )
                        .build();


        ChatMessage savedMessage =
                chatMessageRepository.save(message);


        return ChatMessageResponse.builder()
                .id(savedMessage.getId())
                .classroomId(
                        classroom.getId()
                )
                .senderId(
                        sender.getId()
                )
                .senderName(
                        sender.getFirstName()
                                + " "
                                + sender.getLastName()
                )
                .content(
                        savedMessage.getContent()
                )
                .messageType(
                        savedMessage.getMessageType()
                )
                .createdAt(
                        savedMessage.getCreatedAt()
                )
                .build();
    }


    // =========================================================
    // PRIVATE CHAT HISTORY
    // =========================================================

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getPrivateConversation(
            Long classroomId,
            Long otherUserId,
            AuthenticatedUser authenticatedUser) {

        if (authenticatedUser == null) {

            throw new IllegalStateException(
                    "Authenticated user not available"
            );
        }


        Long currentUserId =
                authenticatedUser.getUserId();

        Long institutionId =
                authenticatedUser.getInstitutionId();


        Classroom classroom =
                classroomRepository
                        .findByIdAndInstitutionId(
                                classroomId,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Classroom not found"
                                ));


        userRepository
                .findByIdAndInstitutionId(
                        otherUserId,
                        institutionId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));


        List<ChatMessage> messages =
                chatMessageRepository
                        .findPrivateConversation(
                                classroomId,
                                currentUserId,
                                otherUserId
                        );


        return messages.stream()
                .map(message ->
                        ChatMessageResponse.builder()
                                .id(message.getId())
                                .classroomId(
                                        message.getClassroom().getId()
                                )
                                .senderId(
                                        message.getSender().getId()
                                )
                                .senderName(
                                        message.getSender().getFirstName()
                                                + " "
                                                + message.getSender().getLastName()
                                )
                                .content(
                                        message.getContent()
                                )
                                .messageType(
                                        message.getMessageType()
                                )
                                .createdAt(
                                        message.getCreatedAt()
                                )
                                .build()
                )
                .toList();
    }


    // =========================================================
    // AUTHENTICATION HELPER
    // =========================================================

    private AuthenticatedUser extractAuthenticatedUser(
            Principal principal) {

        if (principal == null) {

            throw new IllegalStateException(
                    "No authenticated WebSocket principal"
            );
        }


        if (!(principal instanceof Authentication authentication)) {

            throw new IllegalStateException(
                    "Invalid WebSocket principal: "
                            + principal.getClass().getName()
            );
        }


        Object authenticationPrincipal =
                authentication.getPrincipal();


        if (!(authenticationPrincipal
                instanceof AuthenticatedUser authenticatedUser)) {

            throw new IllegalStateException(
                    "Authenticated user information not available"
            );
        }


        return authenticatedUser;
    }
}