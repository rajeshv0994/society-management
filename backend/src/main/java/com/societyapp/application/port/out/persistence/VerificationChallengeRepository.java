package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.VerificationChallenge;
import java.util.UUID;

public interface VerificationChallengeRepository
        extends CrudRepositoryPort<VerificationChallenge, UUID> {}
