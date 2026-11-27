package com.bank.config;

import com.bank.service.PinHashMigrationService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PinHashMigrationRunner implements ApplicationRunner {

    private final PinHashMigrationService pinHashMigrationService;

    public PinHashMigrationRunner(PinHashMigrationService pinHashMigrationService) {
        this.pinHashMigrationService = pinHashMigrationService;
    }

    @Override
    public void run(ApplicationArguments args) {
        pinHashMigrationService.hashLegacyPins();
    }
}