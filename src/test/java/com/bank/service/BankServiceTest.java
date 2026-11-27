package com.bank.service;

import com.bank.config.PinSecurityConfiguration;
import com.bank.exception.BankTransactionException;
import com.bank.exception.PinVerificationException;
import com.bank.model.AccountEntity;
import com.bank.model.Currency;
import com.bank.support.InMemoryRepositories;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankServiceTest {

    private static final String TEST_PIN = "4826";
    private static final Instant TEST_NOW = Instant.parse("2025-01-01T00:00:00Z");

    private final PasswordEncoder passwordEncoder = new PinSecurityConfiguration().pinPasswordEncoder();
    private final Clock clock = Clock.fixed(TEST_NOW, ZoneOffset.UTC);
    private InMemoryRepositories repositories;
    private BankService bankService;

    @BeforeEach
    void setUp() {
        repositories = new InMemoryRepositories();
        bankService = new BankService(
                repositories.accountRepository(),
                repositories.transactionRepository(),
                passwordEncoder,
                clock
        );
    }

    @Test
    void accountCreationStoresBcryptHashInsteadOfSubmittedPin() {
        AccountEntity account = createAccount(new BigDecimal("100.00"));

        assertNotEquals(TEST_PIN, account.getPinHash());
        assertTrue(account.getPinHash().startsWith("$2a$12$"));
        assertTrue(passwordEncoder.matches(TEST_PIN, account.getPinHash()));
    }

    @Test
    void correctPinAllowsWithdrawalAndIncorrectPinDoesNot() throws Exception {
        AccountEntity account = createAccount(new BigDecimal("100.00"));

        PinVerificationException failure = assertThrows(
                PinVerificationException.class,
                () -> bankService.withdraw(account.getAccountNumber(), "1111", new BigDecimal("10.00"))
        );

        assertFalse(failure.getMessage().contains(TEST_PIN));
        assertEquals(1, account.getFailedPinAttempts());
        assertEquals(0, new BigDecimal("100.00").compareTo(account.getBalance()));

        bankService.withdraw(account.getAccountNumber(), TEST_PIN, new BigDecimal("10.00"));

        assertEquals(0, new BigDecimal("90.00").compareTo(account.getBalance()));
        assertEquals(0, account.getFailedPinAttempts());
        assertEquals(1, repositories.transactions().size());
    }

    @Test
    void rejectsInvalidPinFormatOnAccountCreation() {
        assertThrows(
                BankTransactionException.class,
                () -> bankService.createAccount(
                        "Test User",
                        "48x6",
                        BigDecimal.ZERO,
                        Currency.AMD
                )
        );
    }

    @Test
    void invalidSubmittedPinCountsTowardPersistentAccountLimit() throws Exception {
        AccountEntity account = createAccount(BigDecimal.ZERO);

        for (int attempt = 1; attempt <= 4; attempt++) {
            assertThrows(
                    PinVerificationException.class,
                    () -> bankService.withdraw(account.getAccountNumber(), "not-a-pin", BigDecimal.ONE)
            );
        }

        bankService = new BankService(
                repositories.accountRepository(),
                repositories.transactionRepository(),
                passwordEncoder,
                clock
        );
        assertThrows(
                PinVerificationException.class,
                () -> bankService.withdraw(account.getAccountNumber(), "not-a-pin", BigDecimal.ONE)
        );

        assertEquals(5, account.getFailedPinAttempts());
        assertEquals(TEST_NOW.plusSeconds(15 * 60), account.getPinLockedUntil());

        bankService = new BankService(
                repositories.accountRepository(),
                repositories.transactionRepository(),
                passwordEncoder,
                clock
        );
        assertThrows(
                PinVerificationException.class,
                () -> bankService.withdraw(account.getAccountNumber(), TEST_PIN, BigDecimal.ONE)
        );
        assertEquals(5, account.getFailedPinAttempts());
    }

    @Test
    void accountJsonDoesNotExposePinHashOrAttemptState() throws Exception {
        AccountEntity account = createAccount(BigDecimal.ZERO);
        String json = new ObjectMapper().writeValueAsString(account);

        assertFalse(json.contains(account.getPinHash()));
        assertFalse(json.contains("pinHash"));
        assertFalse(json.contains("failedPinAttempts"));
        assertFalse(json.contains("pinLockedUntil"));
        assertFalse(account.toString().contains(account.getPinHash()));
    }

    @Test
    void convertsBetweenConfiguredCurrencies() {
        BigDecimal converted = bankService.convertCurrency(
                new BigDecimal("10.00"),
                Currency.USD,
                Currency.EUR
        );

        assertEquals(0, new BigDecimal("9.25").compareTo(converted));
    }

    @Test
    void rejectsZeroDepositAndTransferToSameAccount() {
        assertThrows(
                BankTransactionException.class,
                () -> bankService.deposit("AM1234567890", BigDecimal.ZERO)
        );
        assertThrows(
                BankTransactionException.class,
                () -> bankService.transfer(
                        "AM1234567890",
                        "AM1234567890",
                        TEST_PIN,
                        BigDecimal.ONE
                )
        );
    }

    private AccountEntity createAccount(BigDecimal initialBalance) {
        return bankService.createAccount("Test User", TEST_PIN, initialBalance, Currency.AMD);
    }
}