package com.projects.EduConflux.files.repository;

import com.projects.EduConflux.files.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository
        extends JpaRepository<StoredFile, Long> {
}