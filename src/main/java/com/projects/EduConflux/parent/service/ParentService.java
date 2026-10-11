
package com.projects.EduConflux.parent.service;

import com.projects.EduConflux.attendance.dto.AttendanceResponse;
import com.projects.EduConflux.attendance.entity.Attendance;
import com.projects.EduConflux.attendance.entity.AttendanceStatus;
import com.projects.EduConflux.attendance.repository.AttendanceRepository;
import com.projects.EduConflux.attendance.service.AttendanceService;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.identity.dto.UserRequest;
import com.projects.EduConflux.identity.dto.UserResponse;
import com.projects.EduConflux.identity.entity.User;
import com.projects.EduConflux.identity.repository.UserRepository;
import com.projects.EduConflux.identity.service.UserService;
import com.projects.EduConflux.institution.entity.Institution;
import com.projects.EduConflux.institution.repository.InstitutionRepository;
import com.projects.EduConflux.parent.dto.LinkStudentRequest;
import com.projects.EduConflux.parent.dto.ParentAttendanceSummaryResponse;
import com.projects.EduConflux.parent.dto.ParentRequest;
import com.projects.EduConflux.parent.dto.ParentResponse;
import com.projects.EduConflux.parent.entity.Parent;
import com.projects.EduConflux.parent.entity.StudentParent;
import com.projects.EduConflux.parent.repository.ParentRepository;
import com.projects.EduConflux.parent.repository.StudentParentRepository;
import com.projects.EduConflux.student.entity.Student;
import com.projects.EduConflux.student.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final ParentRepository parentRepository;
    private final StudentParentRepository studentParentRepository;
    private final StudentRepository studentRepository;
    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceService attendanceService;

    // Create a parent account and link it to the initial student.
    @Transactional
    public ParentResponse createParent(ParentRequest request) {

        Long institutionId = SecurityUtils.getCurrentInstitutionId();

        Institution institution = institutionRepository
                .findById(institutionId)
                .orElseThrow(() ->
                        new RuntimeException("Institution not found"));

        Student student = studentRepository
                .findByIdAndInstitutionId(
                        request.getStudentId(),
                        institutionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Student not found or does not belong to your institution"));

        if (userRepository.findByInstitutionIdAndEmail(
                institutionId, request.getEmail()).isPresent()) {
            throw new RuntimeException(
                    "A user with this institutional email already exists");
        }

        UserRequest userRequest = new UserRequest();
        userRequest.setEmail(request.getEmail());
        userRequest.setPersonalEmail(request.getPersonalEmail());
        userRequest.setFirstName(request.getFirstName());
        userRequest.setLastName(request.getLastName());
        userRequest.setPhone(request.getPhone());
        userRequest.setRole("PARENT");

        UserResponse createdUser = userService.createUser(userRequest);

        User user = userRepository
                .findByIdAndInstitutionId(
                        createdUser.getId(), institutionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Created parent user could not be found"));

        Parent parent = Parent.builder()
                .institution(institution)
                .user(user)
                .occupation(request.getOccupation())
                .address(request.getAddress())
                .build();

        Parent savedParent = parentRepository.save(parent);

        StudentParent link = StudentParent.builder()
                .institution(institution)
                .student(student)
                .parent(savedParent)
                .relationship(request.getRelationship())
                .build();

        studentParentRepository.save(link);

        return toResponse(savedParent, student, link.getRelationship());
    }

    // Link another student to an existing parent.
    @Transactional
    public ParentResponse linkStudent(
            Long parentId,
            LinkStudentRequest request) {

        Long institutionId = SecurityUtils.getCurrentInstitutionId();

        Parent parent = parentRepository
                .findByIdAndInstitutionId(parentId, institutionId)
                .orElseThrow(() ->
                        new RuntimeException("Parent not found"));

        Student student = studentRepository
                .findByIdAndInstitutionId(
                        request.getStudentId(), institutionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Student not found or does not belong to your institution"));

        if (studentParentRepository
                .existsByParentIdAndStudentIdAndInstitutionId(
                        parentId, student.getId(), institutionId)) {
            throw new RuntimeException(
                    "This student is already linked to this parent");
        }

        StudentParent link = StudentParent.builder()
                .institution(parent.getInstitution())
                .student(student)
                .parent(parent)
                .relationship(request.getRelationship())
                .build();

        studentParentRepository.save(link);

        return toResponse(parent, student, link.getRelationship());
    }

    // Admin: retrieve the parents linked to a student.
    @Transactional
    public List<ParentResponse> getParentsForStudent(Long studentId) {

        Long institutionId = SecurityUtils.getCurrentInstitutionId();

        Student student = studentRepository
                .findByIdAndInstitutionId(studentId, institutionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Student not found or does not belong to your institution"));

        return studentParentRepository
                .findByStudentIdAndInstitutionId(studentId, institutionId)
                .stream()
                .map(link -> toResponse(
                        link.getParent(),
                        student,
                        link.getRelationship()))
                .toList();
    }

    // Admin: retrieve the students linked to a parent.
    @Transactional
    public List<ParentResponse> getStudentsForParent(Long parentId) {

        Long institutionId = SecurityUtils.getCurrentInstitutionId();

        Parent parent = parentRepository
                .findByIdAndInstitutionId(parentId, institutionId)
                .orElseThrow(() ->
                        new RuntimeException("Parent not found"));

        return studentParentRepository
                .findByParentIdAndInstitutionId(parentId, institutionId)
                .stream()
                .map(link -> toResponse(
                        parent,
                        link.getStudent(),
                        link.getRelationship()))
                .toList();
    }

    // Parent portal: retrieve only the logged-in parent's students.
    @Transactional
    public List<ParentResponse> getMyStudents() {

        Long institutionId = SecurityUtils.getCurrentInstitutionId();
        Long userId = SecurityUtils.getCurrentUserId();

        Parent parent = getCurrentParent(userId, institutionId);

        return studentParentRepository
                .findByParentIdAndInstitutionId(
                        parent.getId(), institutionId)
                .stream()
                .map(link -> toResponse(
                        parent,
                        link.getStudent(),
                        link.getRelationship()))
                .toList();
    }

    // Parent portal: attendance summary for one linked student.
    @Transactional
    public ParentAttendanceSummaryResponse getStudentAttendanceSummary(
            Long studentId) {

        Long institutionId = SecurityUtils.getCurrentInstitutionId();
        Long userId = SecurityUtils.getCurrentUserId();

        Parent parent = getCurrentParent(userId, institutionId);
        Student student = getLinkedStudent(
                parent, studentId, institutionId);

        List<Attendance> records = attendanceRepository
                .findByStudentIdOrderByAttendanceDateDesc(student.getId());

        long present = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        long absent = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();

        long late = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();



        long total = records.size();

        // Excused classes are excluded from the percentage denominator.
        // Late counts as attended.
        long countedClasses = present + absent + late;

        double percentage = countedClasses == 0
                ? 0.0
                : ((double) (present + late) / countedClasses) * 100.0;

        return ParentAttendanceSummaryResponse.builder()
                .studentId(student.getId())
                .studentName(
                        student.getFirstName() + " " + student.getLastName())
                .totalClasses(total)
                .present(present)
                .absent(absent)
                .late(late)
                .attendancePercentage(
                        Math.round(percentage * 100.0) / 100.0)
                .build();
    }

    // Parent portal: detailed attendance history for one linked student.
    @Transactional
    public List<AttendanceResponse> getStudentAttendanceHistory(
            Long studentId) {

        Long institutionId = SecurityUtils.getCurrentInstitutionId();
        Long userId = SecurityUtils.getCurrentUserId();

        Parent parent = getCurrentParent(userId, institutionId);
        Student student = getLinkedStudent(
                parent, studentId, institutionId);

        return attendanceRepository
                .findByStudentIdOrderByAttendanceDateDesc(student.getId())
                .stream()
                .map(attendanceService::mapToResponse)
                .toList();
    }

    private Parent getCurrentParent(
            Long userId,
            Long institutionId) {

        return parentRepository
                .findByUserIdAndInstitutionId(userId, institutionId)
                .orElseThrow(() ->
                        new RuntimeException("Parent profile not found"));
    }

    private Student getLinkedStudent(
            Parent parent,
            Long studentId,
            Long institutionId) {

        Student student = studentRepository
                .findByIdAndInstitutionId(studentId, institutionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Student not found in your institution"));

        boolean linked = studentParentRepository
                .existsByParentIdAndStudentIdAndInstitutionId(
                        parent.getId(), studentId, institutionId);

        if (!linked) {
            throw new RuntimeException(
                    "You are not authorized to view this student's data");
        }

        return student;
    }

    private ParentResponse toResponse(
            Parent parent,
            Student student,
            String relationship) {

        User user = parent.getUser();

        return ParentResponse.builder()
                .id(parent.getId())
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .personalEmail(user.getPersonalEmail())
                .phone(user.getPhone())
                .relationship(relationship)
                .occupation(parent.getOccupation())
                .address(parent.getAddress())
                .studentId(student.getId())
                .studentName(
                        student.getFirstName() + " " + student.getLastName())
                .enrollmentNumber(student.getEnrollmentNumber())
                .build();
    }
}
