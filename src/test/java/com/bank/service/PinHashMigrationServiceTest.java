package com.bank.service;

import com.bank.config.PinSecurityConfiguration;
import com.bank.model.AccountEntity;
import com.bank.model.Currency;
import com.bank.support.InMemoryRepositories;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PinHashMigrationServiceTest {

    private static final String TEST_PIN = "4826";

    @Test
    void stopsOnUnsupportedStoredValueWithoutEchoingIt() {
        PasswordEncoder encoder = new PinSecurityConfiguration().pinPasswordEncoder();
        InMemoryRepositories repositories = new InMemoryRepositories();
        String unsupportedValue = "unexpected-test-value";
        AccountEntity account = new AccountEntity(
                "AM1234567890",
                "Test User",
                unsupportedValue,
                BigDecimal.ZERO,
                Currency.AMD
        );
        repositories.accountRepository().save(account);

        PinHashMigrationService migration = new PinHashMigrationService(
                repositories.accountRepository(),
                encoder
        );

        IllegalStateException failure = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                migration::hashLegacyPins
        );

        org.junit.jupiter.api.Assertions.assertFalse(failure.getMessage().contains(unsupportedValue));
        assertEquals(unsupportedValue, account.getPinHash());
    }
    @Test
    void migratesLegacyPinInPlaceAndAccountOwnerCanStillUseIt() throws Exception {
        PasswordEncoder encoder = new PinSecurityConfiguration().pinPasswordEncoder();
        InMemoryRepositories repositories = new InMemoryRepositories();
        AccountEntity legacyAccount = new AccountEntity(
                "AM1234567890",
                "Test User",
                TEST_PIN,
                new BigDecimal("25.00"),
                Currency.AMD
        );
        repositories.accountRepository().save(legacyAccount);

        PinHashMigrationService migration = new PinHashMigrationService(
                repositories.accountRepository(),
                encoder
        );

        assertEquals(1, migration.hashLegacyPins());

        String migratedHash = legacyAccount.getPinHash();
        assertFalse(migratedHash.equals(TEST_PIN));
        assertTrue(encoder.matches(TEST_PIN, migratedHash));
        assertEquals(0, migration.hashLegacyPins());
        assertEquals(migratedHash, legacyAccount.getPinHash());

        BankService bankService = new BankService(
                repositories.accountRepository(),
                repositories.transactionRepository(),
                encoder,
                Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC)
        );
        bankService.withdraw(legacyAccount.getAccountNumber(), TEST_PIN, new BigDecimal("5.00"));

        assertEquals(0, new BigDecimal("20.00").compareTo(legacyAccount.getBalance()));
    }
}