package com.projects.EduConflux.timetable.service;

import com.projects.EduConflux.academic.entity.ClassSection;
import com.projects.EduConflux.academic.repository.ClassSectionRepository;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.curriculum.entity.CourseOffering;
import com.projects.EduConflux.curriculum.entity.FacultyCourseAssignment;
import com.projects.EduConflux.curriculum.repository.CourseOfferingRepository;
import com.projects.EduConflux.curriculum.repository.FacultyCourseAssignmentRepository;
import com.projects.EduConflux.timetable.dto.TimetableEntryRequest;
import com.projects.EduConflux.timetable.dto.TimetableEntryResponse;
import com.projects.EduConflux.timetable.entity.TimetableEntry;
import com.projects.EduConflux.timetable.entity.TimetableStatus;
import com.projects.EduConflux.timetable.repository.TimetableEntryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimetableEntryService {

    private final TimetableEntryRepository timetableRepository;

    private final ClassSectionRepository classSectionRepository;

    private final CourseOfferingRepository courseOfferingRepository;

    private final FacultyCourseAssignmentRepository facultyAssignmentRepository;


    public List<TimetableEntryResponse> getAllEntries() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        return timetableRepository
                .findByInstitutionId(institutionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    public TimetableEntryResponse getEntryById(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        TimetableEntry entry =
                timetableRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Timetable entry not found or does not belong to your institution"
                                ));

        return toResponse(entry);
    }


    public List<TimetableEntryResponse> getByClassSection(
            Long classSectionId) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        classSectionRepository
                .findByIdAndInstitutionId(
                        classSectionId,
                        institutionId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Class section not found or does not belong to your institution"
                        ));

        return timetableRepository
                .findByInstitutionIdAndClassSectionId(
                        institutionId,
                        classSectionId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    @Transactional
    public TimetableEntryResponse createEntry(
            TimetableEntryRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();


        /*
         * Validate start/end time.
         */
        validateTime(
                request.getStartTime(),
                request.getEndTime()
        );


        /*
         * Get Class Section.
         */
        ClassSection classSection =
                classSectionRepository
                        .findByIdAndInstitutionId(
                                request.getClassSectionId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Class section not found or does not belong to your institution"
                                ));


        /*
         * Get Course Offering.
         */
        CourseOffering courseOffering =
                courseOfferingRepository
                        .findByIdAndInstitutionId(
                                request.getCourseOfferingId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course offering not found or does not belong to your institution"
                                ));


        /*
         * Get Faculty Assignment.
         */
        FacultyCourseAssignment facultyAssignment =
                facultyAssignmentRepository
                        .findByIdAndFacultyInstitutionId(
                                request.getFacultyAssignmentId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty assignment not found or does not belong to your institution"
                                ));


        /*
         * Course offering must be active.
         */
        if (courseOffering.getStatus()
                != com.projects.EduConflux.curriculum.entity.CourseOfferingStatus.ACTIVE) {

            throw new RuntimeException(
                    "Only active course offerings can be added to timetable"
            );
        }


        /*
         * Faculty assignment must be active.
         */
        if (facultyAssignment.getStatus()
                != com.projects.EduConflux.curriculum.entity.FacultyCourseAssignmentStatus.ACTIVE) {

            throw new RuntimeException(
                    "Only active faculty assignments can be added to timetable"
            );
        }


        /*
         * Faculty assignment must belong
         * to this course offering.
         */
        if (!facultyAssignment
                .getCourseOffering()
                .getId()
                .equals(courseOffering.getId())) {

            throw new RuntimeException(
                    "Faculty assignment does not belong to the selected course offering"
            );
        }


        /*
         * Class section must belong to the
         * same semester as the course.
         */
        if (!classSection
                .getSemester()
                .getId()
                .equals(
                        courseOffering
                                .getCourse()
                                .getSemester()
                                .getId()
                )) {

            throw new RuntimeException(
                    "Class section does not belong to the course semester"
            );
        }


        /*
         * Prevent a class section from having
         * two classes at the same start time.
         */
        if (timetableRepository
                .existsByInstitutionIdAndClassSectionIdAndDayOfWeekAndStartTime(
                        institutionId,
                        classSection.getId(),
                        request.getDayOfWeek(),
                        request.getStartTime()
                )) {

            throw new RuntimeException(
                    "Class section already has a timetable entry at this time"
            );
        }


        TimetableEntry entry =
                TimetableEntry.builder()
                        .institution(
                                classSection.getInstitution()
                        )
                        .classSection(classSection)
                        .courseOffering(courseOffering)
                        .facultyAssignment(facultyAssignment)
                        .dayOfWeek(request.getDayOfWeek())
                        .startTime(request.getStartTime())
                        .endTime(request.getEndTime())
                        .room(request.getRoom())
                        .status(TimetableStatus.ACTIVE)
                        .build();


        TimetableEntry savedEntry =
                timetableRepository.save(entry);


        return toResponse(savedEntry);
    }


    @Transactional
    public TimetableEntryResponse updateEntry(
            Long id,
            TimetableEntryRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();


        TimetableEntry entry =
                timetableRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Timetable entry not found or does not belong to your institution"
                                ));


        validateTime(
                request.getStartTime(),
                request.getEndTime()
        );


        ClassSection classSection =
                classSectionRepository
                        .findByIdAndInstitutionId(
                                request.getClassSectionId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Class section not found or does not belong to your institution"
                                ));


        CourseOffering courseOffering =
                courseOfferingRepository
                        .findByIdAndInstitutionId(
                                request.getCourseOfferingId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course offering not found or does not belong to your institution"
                                ));


        FacultyCourseAssignment facultyAssignment =
                facultyAssignmentRepository
                        .findByIdAndFacultyInstitutionId(
                                request.getFacultyAssignmentId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty assignment not found or does not belong to your institution"
                                ));


        if (courseOffering.getStatus()
                != com.projects.EduConflux.curriculum.entity.CourseOfferingStatus.ACTIVE) {

            throw new RuntimeException(
                    "Only active course offerings can be added to timetable"
            );
        }


        if (facultyAssignment.getStatus()
                != com.projects.EduConflux.curriculum.entity.FacultyCourseAssignmentStatus.ACTIVE) {

            throw new RuntimeException(
                    "Only active faculty assignments can be added to timetable"
            );
        }


        if (!facultyAssignment
                .getCourseOffering()
                .getId()
                .equals(courseOffering.getId())) {

            throw new RuntimeException(
                    "Faculty assignment does not belong to the selected course offering"
            );
        }


        if (!classSection
                .getSemester()
                .getId()
                .equals(
                        courseOffering
                                .getCourse()
                                .getSemester()
                                .getId()
                )) {

            throw new RuntimeException(
                    "Class section does not belong to the course semester"
            );
        }


        boolean changed =
                !entry.getClassSection()
                        .getId()
                        .equals(classSection.getId())
                        ||
                        !entry.getDayOfWeek()
                                .equals(request.getDayOfWeek())
                        ||
                        !entry.getStartTime()
                                .equals(request.getStartTime());


        if (changed &&
                timetableRepository
                        .existsByInstitutionIdAndClassSectionIdAndDayOfWeekAndStartTime(
                                institutionId,
                                classSection.getId(),
                                request.getDayOfWeek(),
                                request.getStartTime()
                        )) {

            throw new RuntimeException(
                    "Class section already has a timetable entry at this time"
            );
        }


        entry.setClassSection(classSection);
        entry.setCourseOffering(courseOffering);
        entry.setFacultyAssignment(facultyAssignment);
        entry.setDayOfWeek(request.getDayOfWeek());
        entry.setStartTime(request.getStartTime());
        entry.setEndTime(request.getEndTime());
        entry.setRoom(request.getRoom());


        TimetableEntry updatedEntry =
                timetableRepository.save(entry);


        return toResponse(updatedEntry);
    }


    public TimetableEntryResponse updateStatus(
            Long id,
            TimetableStatus status) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();


        TimetableEntry entry =
                timetableRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Timetable entry not found or does not belong to your institution"
                                ));


        entry.setStatus(status);


        return toResponse(
                timetableRepository.save(entry)
        );
    }


    public void deleteEntry(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();


        TimetableEntry entry =
                timetableRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Timetable entry not found or does not belong to your institution"
                                ));


        timetableRepository.delete(entry);
    }


    private void validateTime(
            LocalTime startTime,
            LocalTime endTime) {

        if (!endTime.isAfter(startTime)) {

            throw new RuntimeException(
                    "End time must be after start time"
            );
        }
    }


    private TimetableEntryResponse toResponse(
            TimetableEntry entry) {

        TimetableEntryResponse response =
                new TimetableEntryResponse();


        response.setId(
                entry.getId()
        );


        response.setInstitutionId(
                entry.getInstitution()
                        .getId()
        );


        ClassSection classSection =
                entry.getClassSection();


        response.setClassSectionId(
                classSection.getId()
        );

        response.setClassSectionName(
                classSection.getName()
        );


        CourseOffering offering =
                entry.getCourseOffering();


        response.setCourseOfferingId(
                offering.getId()
        );


        response.setCourseId(
                offering.getCourse()
                        .getId()
        );

        response.setCourseCode(
                offering.getCourse()
                        .getCode()
        );

        response.setCourseName(
                offering.getCourse()
                        .getName()
        );


        FacultyCourseAssignment assignment =
                entry.getFacultyAssignment();


        response.setFacultyAssignmentId(
                assignment.getId()
        );


        response.setFacultyId(
                assignment
                        .getFaculty()
                        .getId()
        );

        response.setFacultyName(
                assignment
                        .getFaculty()
                        .getFirstName()
                        + " "
                        + assignment
                        .getFaculty()
                        .getLastName()
        );

        response.setFacultyEmployeeId(
                assignment
                        .getFaculty()
                        .getEmployeeId()
        );


        response.setProgramId(
                offering
                        .getCourse()
                        .getProgram()
                        .getId()
        );

        response.setProgramName(
                offering
                        .getCourse()
                        .getProgram()
                        .getName()
        );


        response.setSemesterId(
                offering
                        .getCourse()
                        .getSemester()
                        .getId()
        );

        response.setSemesterNumber(
                offering
                        .getCourse()
                        .getSemester()
                        .getSemesterNumber()
        );

        response.setSemesterName(
                offering
                        .getCourse()
                        .getSemester()
                        .getName()
        );


        response.setAcademicYearId(
                offering
                        .getAcademicYear()
                        .getId()
        );

        response.setAcademicYearName(
                offering
                        .getAcademicYear()
                        .getName()
        );


        response.setDayOfWeek(
                entry.getDayOfWeek()
        );

        response.setStartTime(
                entry.getStartTime()
        );

        response.setEndTime(
                entry.getEndTime()
        );

        response.setRoom(
                entry.getRoom()
        );

        response.setStatus(
                entry.getStatus()
        );

        response.setCreatedAt(
                entry.getCreatedAt()
        );

        response.setUpdatedAt(
                entry.getUpdatedAt()
        );


        return response;
    }
}