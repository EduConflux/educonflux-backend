package com.projects.EduConflux.classroom.invitation.service;

import com.projects.EduConflux.classroom.entity.Classroom;
import com.projects.EduConflux.classroom.entity.ClassroomStatus;
import com.projects.EduConflux.classroom.invitation.dto.CreateInvitationRequest;
import com.projects.EduConflux.classroom.invitation.dto.InvitationResponse;
import com.projects.EduConflux.classroom.invitation.dto.JoinClassroomRequest;
import com.projects.EduConflux.classroom.invitation.entity.ClassroomInvitation;
import com.projects.EduConflux.classroom.invitation.repository.ClassroomInvitationRepository;
import com.projects.EduConflux.classroom.membership.entity.ClassroomMembership;
import com.projects.EduConflux.classroom.membership.entity.MembershipStatus;
import com.projects.EduConflux.classroom.membership.repository.ClassroomMembershipRepository;
import com.projects.EduConflux.classroom.repository.ClassroomRepository;
import com.projects.EduConflux.common.email.EmailService;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.faculty.entity.Faculty;
import com.projects.EduConflux.faculty.repository.FacultyRepository;
import com.projects.EduConflux.identity.entity.User;
import com.projects.EduConflux.identity.repository.UserRepository;
import com.projects.EduConflux.notification.entity.NotificationType;
import com.projects.EduConflux.notification.service.NotificationService;
import com.projects.EduConflux.student.entity.Student;
import com.projects.EduConflux.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassroomInvitationService {

    private final ClassroomInvitationRepository invitationRepository;
    private final ClassroomMembershipRepository membershipRepository;
    private final ClassroomRepository classroomRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    @Value("${educonflux.frontend.url:}")
    private String frontendUrl;

    private final SecureRandom secureRandom =
            new SecureRandom();


    /*
     * Faculty creates an invitation link.
     */
    @Transactional
    public InvitationResponse createInvitation(
            Long classroomId,
            CreateInvitationRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String facultyEmail =
                SecurityUtils.getCurrentUserEmail();


        /*
         * Find authenticated faculty.
         */
        Faculty faculty =
                facultyRepository
                        .findByInstitutionIdAndEmail(
                                institutionId,
                                facultyEmail
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty profile not found"
                                ));


        /*
         * Find classroom belonging to
         * the current institution.
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
         * Only classroom owner can
         * generate invitations.
         */
        if (!classroom
                .getFaculty()
                .getId()
                .equals(faculty.getId())) {

            throw new RuntimeException(
                    "You are not the owner of this classroom"
            );
        }


        /*
         * Archived classrooms cannot
         * receive new invitations.
         */
        if (classroom.getStatus()
                != ClassroomStatus.ACTIVE) {

            throw new RuntimeException(
                    "Classroom is not active"
            );
        }


        /*
         * Generate a secure random token.
         */
        String token =
                generateSecureToken();


        /*
         * Invitation expires after 7 days.
         */
        LocalDateTime expiresAt =
                LocalDateTime.now()
                        .plusDays(7);


        ClassroomInvitation invitation =
                ClassroomInvitation.builder()
                        .classroom(classroom)
                        .token(token)
                        .expiresAt(expiresAt)
                        .active(true)
                        .build();


        invitation =
                invitationRepository.save(
                        invitation
                );

        for (Long studentId : request.getStudentIds()) {

            Student student =
                    studentRepository
                            .findByIdAndInstitutionId(
                                    studentId,
                                    institutionId
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Student not found: "
                                                    + studentId
                                    ));

            emailService.sendClassroomInvitation(
                    student.getPersonalEmail(),
                    student.getFirstName(),
                    classroom.getName(),
                    faculty.getFirstName()
                            + " "
                            + faculty.getLastName(),
                    invitation.getToken(),
                    invitation.getExpiresAt()
                            .toString()
            );

            User user =
                    userRepository
                            .findByInstitutionIdAndEmail(
                                    institutionId,
                                    student.getEmail()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "User account not found for student: "
                                                    + student.getEmail()
                                    ));

            notificationService.createNotification(
                    user,
                    "New Classroom Invitation",
                    faculty.getFirstName()
                            + " "
                            + faculty.getLastName()
                            + " invited you to join "
                            + classroom.getName(),
                    NotificationType.CLASSROOM_INVITATION,
                    invitation.getId()
            );
        }


        return mapToResponse(
                invitation
        );
    }


    /*
     * Student joins classroom using
     * an invitation token.
     */
    @Transactional
    public void joinClassroom(
            JoinClassroomRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String studentEmail =
                SecurityUtils.getCurrentUserEmail();


        /*
         * Find authenticated student.
         */
        Student student =
                studentRepository
                        .findByInstitutionIdAndEmail(
                                institutionId,
                                studentEmail
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student profile not found"
                                ));


        /*
         * Find active invitation.
         */
        ClassroomInvitation invitation =
                invitationRepository
                        .findByTokenAndActiveTrue(
                                request.getToken()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid invitation"
                                ));


        /*
         * Check expiration.
         */
        if (LocalDateTime.now()
                .isAfter(
                        invitation.getExpiresAt()
                )) {

            invitation.setActive(false);

            invitationRepository.save(
                    invitation
            );

            throw new RuntimeException(
                    "Invitation has expired"
            );
        }


        Classroom classroom =
                invitation.getClassroom();


        /*
         * Make sure classroom belongs
         * to the student's institution.
         */
        if (!classroom
                .getInstitution()
                .getId()
                .equals(institutionId)) {

            throw new RuntimeException(
                    "Classroom does not belong to your institution"
            );
        }


        /*
         * Classroom must still be active.
         */
        if (classroom.getStatus()
                != ClassroomStatus.ACTIVE) {

            throw new RuntimeException(
                    "Classroom is not active"
            );
        }


        /*
         * Check whether a membership record already exists.
         */
        Optional<ClassroomMembership> existingMembership =
                membershipRepository
                        .findByClassroomIdAndStudentId(
                                classroom.getId(),
                                student.getId()
                        );

        if (existingMembership.isPresent()) {

            ClassroomMembership membership =
                    existingMembership.get();

            /*
             * ACTIVE means the student is already
             * inside the classroom.
             */
            if (membership.getStatus()
                    == MembershipStatus.ACTIVE) {

                throw new RuntimeException(
                        "You are already a member of this classroom"
                );
            }

            /*
             * REMOVED means the student was previously
             * a member but was removed by the faculty.
             *
             * Instead of creating another database row,
             * reactivate the existing membership.
             */
            membership.setStatus(
                    MembershipStatus.ACTIVE
            );

            membership.setRemovedAt(null);

            membershipRepository.save(
                    membership
            );

            return;
        }


        /*
         * Create membership.
         */
        ClassroomMembership membership =
                ClassroomMembership.builder()
                        .classroom(classroom)
                        .student(student)
                        .status(
                                MembershipStatus.ACTIVE
                        )
                        .build();


        membershipRepository.save(
                membership
        );
    }


    /*
     * Get invitations created for
     * a classroom.
     */
    @Transactional(readOnly = true)
    public List<InvitationResponse>
    getClassroomInvitations(
            Long classroomId) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String facultyEmail =
                SecurityUtils.getCurrentUserEmail();


        Faculty faculty =
                facultyRepository
                        .findByInstitutionIdAndEmail(
                                institutionId,
                                facultyEmail
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty profile not found"
                                ));


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


        if (!classroom
                .getFaculty()
                .getId()
                .equals(faculty.getId())) {

            throw new RuntimeException(
                    "You are not the owner of this classroom"
            );
        }


        return invitationRepository
                .findByClassroomId(
                        classroomId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    /*
     * Generate a URL-safe random token.
     */
    private String generateSecureToken() {

        byte[] randomBytes =
                new byte[32];

        secureRandom.nextBytes(
                randomBytes
        );

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        randomBytes
                );
    }


    private InvitationResponse mapToResponse(
            ClassroomInvitation invitation) {

        return InvitationResponse.builder()
                .id(invitation.getId())
                .classroomId(
                        invitation
                                .getClassroom()
                                .getId()
                )
                .classroomName(
                        invitation
                                .getClassroom()
                                .getName()
                )
                .token(
                        invitation.getToken()
                )
                .invitationLink(
                        "http://localhost:3000/classroom/join/"
                                + invitation.getToken()
                )
                .createdAt(
                        invitation.getCreatedAt()
                )
                .expiresAt(
                        invitation.getExpiresAt()
                )
                .active(
                        invitation.isActive()
                )
                .build();
    }
}