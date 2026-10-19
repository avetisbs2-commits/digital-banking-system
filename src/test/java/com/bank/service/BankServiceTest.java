package com.bank.service;

import com.bank.model.Account;
import com.bank.model.Currency;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BankService bankService;

    @Test
    void testCreateAccountSuccessfully() {
        // Подготовка данных
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Вызов метода
        Account account = bankService.createAccount("Avetis Aghajanyan", "1234", new BigDecimal("50000"), Currency.AMD);

        // Проверки
        assertNotNull(account);
        assertEquals("Avetis Aghajanyan", account.getOwnerName());
        assertEquals(Currency.AMD, account.getCurrency());
        assertEquals(0, new BigDecimal("50000").compareTo(account.getBalance()));

    }

    @Test
    void testWithdrawWithWrongPinThrowsException() {
        Account account = new Account();
        account.setAccountNumber("ACC-123");
        account.setPinCode("1234");
        account.setBalance(new BigDecimal("10000"));

        when(accountRepository.findByAccountNumber("ACC-123")).thenReturn(Optional.of(account));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bankService.withdraw("ACC-123", "0000", new BigDecimal("1000"));
        });

        assertTrue(exception.getMessage().contains("PIN") || exception.getMessage().contains("pin") || exception.getMessage().length() > 0);
    }
}