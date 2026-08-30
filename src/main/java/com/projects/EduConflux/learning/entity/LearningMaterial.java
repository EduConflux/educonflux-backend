package com.projects.EduConflux.learning.entity;

import com.projects.EduConflux.classroom.entity.Classroom;
import com.projects.EduConflux.faculty.entity.Faculty;
import com.projects.EduConflux.files.entity.StoredFile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "learning_materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /*
     * Classroom where this material
     * is available.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "classroom_id",
            nullable = false
    )
    private Classroom classroom;


    /*
     * Faculty who created the material.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "faculty_id",
            nullable = false
    )
    private Faculty faculty;


    /*
     * Uploaded file.
     *
     * Optional because a learning material
     * can contain only text.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private StoredFile file;


    /*
     * Title of the material.
     */
    @Column(nullable = false, length = 200)
    private String title;


    /*
     * Description or content.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;


    /*
     * Creation time.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    /*
     * Last update time.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }


    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }
}