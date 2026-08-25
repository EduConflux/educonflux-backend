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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;

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

    @Transactional
    public ChatMessageResponse sendGroupMessage(
            Long classroomId,
            String content,
            AuthenticatedUser authenticatedUser) {

        if (authenticatedUser == null) {

            throw new IllegalStateException(
                    "Authenticated WebSocket user not available"
            );
        }

        Long userId =
                authenticatedUser.getUserId();

        Long institutionId =
                authenticatedUser.getInstitutionId();

        String email =
                authenticatedUser.getEmail();

        List<String> roles =
                authenticatedUser.getRoles();

        /*
         * Resolve authenticated User.
         */
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

        /*
         * Find classroom inside the
         * authenticated user's institution.
         */
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

        /*
         * STUDENT
         *
         * Student must have an ACTIVE
         * membership in the classroom.
         */
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

        /*
         * FACULTY
         *
         * Only the faculty who owns the
         * classroom can send messages.
         */
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

        /*
         * Any other role cannot use
         * classroom group chat.
         */
        else {

            throw new RuntimeException(
                    "You are not allowed to send messages"
            );
        }

        /*
         * Create classroom-wide message.
         *
         * recipient = null means
         * everyone in the classroom.
         */
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

        /*
         * Return response.
         *
         * @SendTo in the controller will
         * broadcast this response to:
         *
         * /topic/classroom/{classroomId}/chat
         */
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

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatHistory(
            Long classroomId,
            Principal principal) {

        if (principal == null) {
            throw new IllegalStateException(
                    "No authenticated user"
            );
        }

        AuthenticatedUser authenticatedUser;

        if (principal instanceof Authentication authentication) {

            Object authenticationPrincipal =
                    authentication.getPrincipal();

            if (!(authenticationPrincipal
                    instanceof AuthenticatedUser)) {

                throw new IllegalStateException(
                        "Authenticated user information not available"
                );
            }

            authenticatedUser =
                    (AuthenticatedUser)
                            authenticationPrincipal;

        } else if (principal instanceof AuthenticatedUser user) {

            authenticatedUser = user;

        } else {

            throw new IllegalStateException(
                    "Unsupported principal: "
                            + principal.getClass().getName()
            );
        }

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
                                .content(message.getContent())
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
}