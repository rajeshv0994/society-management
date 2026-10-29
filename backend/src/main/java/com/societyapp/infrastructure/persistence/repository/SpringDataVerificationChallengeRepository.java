package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.VerificationChallenge;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataVerificationChallengeRepository
        extends JpaRepository<VerificationChallenge, UUID>,
                com.societyapp.application.port.out.persistence.VerificationChallengeRepository {}
