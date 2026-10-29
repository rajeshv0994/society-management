package com.societyapp.infrastructure.bootstrap;

import com.societyapp.application.model.storage.StorageModels;
import com.societyapp.application.port.in.StorageMeterUseCase;
import com.societyapp.documents.LocalDocumentStorage;
import org.slf4j.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class StorageReconciliationRunner implements ApplicationRunner, Ordered {
    private static final Logger log = LoggerFactory.getLogger(StorageReconciliationRunner.class);
    private final LocalDocumentStorage storage;
    private final StorageMeterUseCase meter;

    public StorageReconciliationRunner(LocalDocumentStorage storage, StorageMeterUseCase meter) {
        this.storage = storage;
        this.meter = meter;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        int count = 0;
        for (var file : storage.inventory()) {
            meter.registerExisting(
                    new StorageModels.RegisterExistingCommand(
                            file.societyId(),
                            file.storageKey(),
                            file.category(),
                            file.contentType(),
                            file.sizeBytes()));
            count++;
        }
        log.info("Reconciled {} stored files with the quota ledger", count);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
