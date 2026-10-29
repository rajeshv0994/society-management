package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.StaffProfile;
import java.util.UUID;

public interface StaffProfileRepository extends CrudRepositoryPort<StaffProfile, UUID> {}
