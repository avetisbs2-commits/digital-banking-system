package com.bank.controller;

import com.bank.config.PinSecurityConfiguration;
import com.bank.model.AccountEntity;
import com.bank.model.Currency;
import com.bank.service.BankService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class WebControllerTest {

    @Test
    void bankingDepositEndpointStillWorks() throws Exception {
        StubBankService bankService = new StubBankService();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new WebController(bankService)).build();

        mockMvc.perform(post("/account/deposit")
                        .param("accountNumber", "AM1234567890")
                        .param("amount", "10.00"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        assertEquals("AM1234567890", bankService.depositedAccountNumber);
        assertEquals(new BigDecimal("10.00"), bankService.depositedAmount);
    }

    @Test
    void accountCreationResponseDoesNotContainPinOrHash() throws Exception {
        StubBankService bankService = new StubBankService();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new WebController(bankService)).build();

        mockMvc.perform(post("/account/create")
                        .param("ownerName", "Test User")
                        .param("pinCode", "4826")
                        .param("initialBalance", "0.00")
                        .param("currency", "AMD"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    org.junit.jupiter.api.Assertions.assertFalse(body.contains("4826"));
                    org.junit.jupiter.api.Assertions.assertFalse(body.contains(bankService.returnedAccount.getPinHash()));
                });
    }

    private static class StubBankService extends BankService {
        private String depositedAccountNumber;
        private BigDecimal depositedAmount;
        private AccountEntity returnedAccount;

        StubBankService() {
            super(
                    null,
                    null,
                    new PinSecurityConfiguration().pinPasswordEncoder(),
                    Clock.systemUTC()
            );
        }

        @Override
        public void deposit(String accountNumber, BigDecimal amount) {
            depositedAccountNumber = accountNumber;
            depositedAmount = amount;
        }

        @Override
        public AccountEntity createAccount(String ownerName, String pinCode, BigDecimal initialBalance, Currency currency) {
            String fakeHash = new PinSecurityConfiguration()
                    .pinPasswordEncoder()
                    .encode(pinCode);
            returnedAccount = new AccountEntity("AM1234567890", ownerName, fakeHash, initialBalance, currency);
            return returnedAccount;
        }
    }
}