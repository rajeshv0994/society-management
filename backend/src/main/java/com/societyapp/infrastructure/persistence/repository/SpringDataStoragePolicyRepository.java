package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.StoragePolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataStoragePolicyRepository
        extends JpaRepository<StoragePolicy, Short>,
                com.societyapp.application.port.out.persistence.StoragePolicyRepository {}
