package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.PlatformServiceModule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPlatformServiceModuleRepository
        extends JpaRepository<PlatformServiceModule, String>,
                com.societyapp.application.port.out.persistence.PlatformServiceModuleRepository {
    List<PlatformServiceModule> findAllByOrderBySortOrderAscNameAsc();

    List<PlatformServiceModule> findByStatusOrderBySortOrderAscNameAsc(String status);
}
