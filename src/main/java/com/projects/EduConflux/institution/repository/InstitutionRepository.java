package com.projects.EduConflux.institution.repository;

import com.projects.EduConflux.institution.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;


public interface InstitutionRepository extends JpaRepository<Institution, Long> {

}