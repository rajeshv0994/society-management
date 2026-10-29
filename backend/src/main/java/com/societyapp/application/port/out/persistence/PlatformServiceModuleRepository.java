package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.PlatformServiceModule;
import java.util.List;

public interface PlatformServiceModuleRepository
        extends CrudRepositoryPort<PlatformServiceModule, String> {
    List<PlatformServiceModule> findAllByOrderBySortOrderAscNameAsc();

    List<PlatformServiceModule> findByStatusOrderBySortOrderAscNameAsc(String status);
}
