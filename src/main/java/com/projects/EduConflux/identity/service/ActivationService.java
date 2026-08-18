package com.projects.EduConflux.identity.service;

import com.projects.EduConflux.identity.entity.ActivationToken;
import com.projects.EduConflux.identity.entity.User;
import com.projects.EduConflux.identity.entity.UserStatus;
import com.projects.EduConflux.identity.repository.ActivationTokenRepository;
import com.projects.EduConflux.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivationService {

    private final ActivationTokenRepository activationTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public String createActivationToken(User user) {

        // Remove any existing token for this user
        activationTokenRepository.findByUserId(user.getId())
                .ifPresent(activationTokenRepository::delete);

        String token = UUID.randomUUID().toString();

        ActivationToken activationToken = ActivationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();

        activationTokenRepository.save(activationToken);

        return token;
    }

    @Transactional
    public void activateAccount(String token) {

        ActivationToken activationToken =
                activationTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid activation token"
                                ));

        if (activationToken.isUsed()) {
            throw new RuntimeException(
                    "Activation token has already been used"
            );
        }

        if (activationToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Activation token has expired"
            );
        }

        User user = activationToken.getUser();

        if (user.getStatus() != UserStatus.PENDING) {
            throw new RuntimeException(
                    "User account is not pending activation"
            );
        }

        user.setStatus(UserStatus.ACTIVE);

        activationToken.setUsed(true);

        userRepository.save(user);
        activationTokenRepository.save(activationToken);
    }
}