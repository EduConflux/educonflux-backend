package com.projects.EduConflux.identity.repository;

import com.projects.EduConflux.identity.entity.ActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivationTokenRepository
        extends JpaRepository<ActivationToken, Long> {

    Optional<ActivationToken> findByToken(String token);

    Optional<ActivationToken> findByUserId(Long userId);

    boolean existsByToken(String token);
}