package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.SetupCode;
import java.util.*;

public interface SetupCodeRepository extends CrudRepositoryPort<SetupCode, UUID> {
    List<SetupCode> findByStatusOrderByCreatedAtDesc(String status);
}
