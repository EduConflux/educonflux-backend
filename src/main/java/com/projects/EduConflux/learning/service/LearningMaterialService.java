package com.projects.EduConflux.learning.service;

import com.projects.EduConflux.classroom.entity.Classroom;
import com.projects.EduConflux.classroom.membership.entity.ClassroomMembership;
import com.projects.EduConflux.classroom.membership.entity.MembershipStatus;
import com.projects.EduConflux.classroom.membership.repository.ClassroomMembershipRepository;
import com.projects.EduConflux.classroom.repository.ClassroomRepository;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.faculty.entity.Faculty;
import com.projects.EduConflux.faculty.repository.FacultyRepository;
import com.projects.EduConflux.files.entity.StoredFile;
import com.projects.EduConflux.files.repository.StoredFileRepository;
import com.projects.EduConflux.identity.entity.User;
import com.projects.EduConflux.identity.repository.UserRepository;
import com.projects.EduConflux.learning.dto.CreateLearningMaterialRequest;
import com.projects.EduConflux.learning.dto.LearningMaterialResponse;
import com.projects.EduConflux.learning.entity.LearningMaterial;
import com.projects.EduConflux.learning.repository.LearningMaterialRepository;
import com.projects.EduConflux.notification.entity.NotificationType;
import com.projects.EduConflux.notification.service.NotificationService;
import com.projects.EduConflux.student.entity.Student;
import com.projects.EduConflux.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LearningMaterialService {

    private final LearningMaterialRepository materialRepository;
    private final ClassroomRepository classroomRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final ClassroomMembershipRepository membershipRepository;
    private final StoredFileRepository storedFileRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;


    /*
     * FACULTY
     *
     * Create learning material.
     */
    @Transactional
    public LearningMaterialResponse createMaterial(
            Long classroomId,
            CreateLearningMaterialRequest request) {

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
         * Find classroom.
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
         * create learning material.
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
         * Find uploaded file if provided.
         */
        StoredFile file = null;

        if (request.getFileId() != null) {

            file =
                    storedFileRepository
                            .findById(
                                    request.getFileId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "File not found"
                                    ));


            /*
             * Make sure faculty uploaded
             * this file.
             */
            if (!file
                    .getUploadedBy()
                    .getId()
                    .equals(
                            SecurityUtils.getCurrentUserId()
                    )) {

                throw new RuntimeException(
                        "You cannot attach this file"
                );
            }
        }


        /*
         * Create material.
         */
        LearningMaterial material =
                LearningMaterial.builder()
                        .classroom(classroom)
                        .faculty(faculty)
                        .file(file)
                        .title(request.getTitle())
                        .description(
                                request.getDescription()
                        )
                        .build();


        LearningMaterial saved =
                materialRepository.save(
                        material
                );


        /*
         * Create notifications for all
         * active students in the classroom.
         */
        var memberships =
                membershipRepository
                        .findByClassroomIdAndStatus(
                                classroomId,
                                MembershipStatus.ACTIVE
                        );


        for (ClassroomMembership membership :
                memberships) {

            Student student =
                    membership.getStudent();


            /*
             * Find the User account belonging
             * to the student.
             */
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


            /*
             * Create learning material
             * notification.
             */
            notificationService.createNotification(
                    user,
                    "New Learning Material",
                    "New learning material \"" +
                            saved.getTitle() +
                            "\" has been added to " +
                            classroom.getName(),
                    NotificationType.LEARNING_MATERIAL,
                    saved.getId()
            );
        }


        return mapToResponse(saved);
    }


    /*
     * STUDENT
     *
     * View learning materials.
     */
    @Transactional(readOnly = true)
    public Page<LearningMaterialResponse>
    getStudentMaterials(
            Long classroomId,
            Pageable pageable) {

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
         * Check membership.
         */
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


        /*
         * Student must be active.
         */
        if (membership.getStatus()
                != MembershipStatus.ACTIVE) {

            throw new RuntimeException(
                    "You are not an active member of this classroom"
            );
        }


        return materialRepository
                .findByClassroomId(
                        classroomId,
                        pageable
                )
                .map(this::mapToResponse);
    }


    /*
     * FACULTY
     *
     * View learning materials.
     */
    @Transactional(readOnly = true)
    public Page<LearningMaterialResponse>
    getFacultyMaterials(
            Long classroomId,
            Pageable pageable) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String facultyEmail =
                SecurityUtils.getCurrentUserEmail();


        /*
         * Find faculty.
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
         * Find classroom.
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
         * Only classroom owner can access.
         */
        if (!classroom
                .getFaculty()
                .getId()
                .equals(faculty.getId())) {

            throw new RuntimeException(
                    "You are not the owner of this classroom"
            );
        }


        return materialRepository
                .findByClassroomId(
                        classroomId,
                        pageable
                )
                .map(this::mapToResponse);
    }

    /*
     * FACULTY
     *
     * Update learning material.
     */
    @Transactional
    public LearningMaterialResponse updateMaterial(
            Long materialId,
            CreateLearningMaterialRequest request) {

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
         * Find learning material.
         */
        LearningMaterial material =
                materialRepository
                        .findById(materialId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Learning material not found"
                                ));


        /*
         * Only the faculty who created
         * the material can update it.
         */
        if (!material
                .getFaculty()
                .getId()
                .equals(faculty.getId())) {

            throw new RuntimeException(
                    "You are not the owner of this learning material"
            );
        }


        /*
         * Update title and description.
         */
        material.setTitle(
                request.getTitle()
        );

        material.setDescription(
                request.getDescription()
        );


        /*
         * Update file if a new file
         * was provided.
         */
        if (request.getFileId() != null) {

            StoredFile file =
                    storedFileRepository
                            .findById(
                                    request.getFileId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "File not found"
                                    ));


            /*
             * Make sure the faculty uploaded
             * this file.
             */
            if (!file
                    .getUploadedBy()
                    .getId()
                    .equals(
                            SecurityUtils.getCurrentUserId()
                    )) {

                throw new RuntimeException(
                        "You cannot attach this file"
                );
            }


            material.setFile(file);
        }


        LearningMaterial updated =
                materialRepository.save(
                        material
                );


        return mapToResponse(updated);
    }


    /*
     * FACULTY
     *
     * Delete learning material.
     */
    @Transactional
    public void deleteMaterial(
            Long materialId) {

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
         * Find learning material.
         */
        LearningMaterial material =
                materialRepository
                        .findById(materialId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Learning material not found"
                                ));


        /*
         * Only the faculty who created
         * the material can delete it.
         */
        if (!material
                .getFaculty()
                .getId()
                .equals(faculty.getId())) {

            throw new RuntimeException(
                    "You are not the owner of this learning material"
            );
        }


        materialRepository.delete(material);
    }


    /*
     * Convert entity to response.
     */
    private LearningMaterialResponse mapToResponse(
            LearningMaterial material) {

        Classroom classroom =
                material.getClassroom();

        Faculty faculty =
                material.getFaculty();


        return LearningMaterialResponse.builder()

                .id(
                        material.getId()
                )

                .classroomId(
                        classroom.getId()
                )

                .classroomName(
                        classroom.getName()
                )

                .facultyId(
                        faculty.getId()
                )

                .facultyName(
                        faculty.getFirstName()
                                + " "
                                + faculty.getLastName()
                )

                .title(
                        material.getTitle()
                )

                .description(
                        material.getDescription()
                )

                .fileId(
                        material.getFile() != null
                                ? material.getFile().getId()
                                : null
                )

                .fileName(
                        material.getFile() != null
                                ? material.getFile().getOriginalName()
                                : null
                )

                .createdAt(
                        material.getCreatedAt()
                )

                .updatedAt(
                        material.getUpdatedAt()
                )

                .build();
    }
}