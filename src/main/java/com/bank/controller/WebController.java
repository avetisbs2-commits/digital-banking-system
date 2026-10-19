package com.bank.controller;

import com.bank.model.Account;
import com.bank.model.Currency;
import com.bank.model.Transaction;
import com.bank.model.TransactionType;
import com.bank.service.BankService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class WebController {

    private final BankService bankService;

    public WebController(BankService bankService) {
        this.bankService = bankService;
    }

    // Главная страница
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // 1. Создание счета
    @PostMapping("/account/create")
    public String createAccount(@RequestParam String ownerName,
                                @RequestParam String pinCode,
                                @RequestParam BigDecimal initialBalance,
                                @RequestParam Currency currency,
                                Model model) {
        try {
            Account acc = bankService.createAccount(ownerName, pinCode, initialBalance, currency);
            model.addAttribute("message", "Account created successfully! Number: " + acc.getAccountNumber());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "index";
    }

    // 2. Пополнение
    @PostMapping("/account/deposit")
    public String deposit(@RequestParam String accountNumber,
                          @RequestParam BigDecimal amount,
                          Model model) {
        try {
            bankService.deposit(accountNumber, amount);
            model.addAttribute("message", "Account " + accountNumber + " deposited successfully with " + amount);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "index";
    }

    // 3. Снятие
    @PostMapping("/account/withdraw")
    public String withdraw(@RequestParam String accountNumber,
                           @RequestParam String pinCode,
                           @RequestParam BigDecimal amount,
                           Model model) {
        try {
            bankService.withdraw(accountNumber, pinCode, amount);
            model.addAttribute("message", "Withdrawn " + amount + " from account " + accountNumber + " successfully!");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "index";
    }

    // 4. Перевод
    @PostMapping("/account/transfer")
    public String transfer(@RequestParam String senderNumber,
                           @RequestParam String receiverNumber,
                           @RequestParam String pinCode,
                           @RequestParam BigDecimal amount,
                           Model model) {
        try {
            bankService.transfer(senderNumber, receiverNumber, pinCode, amount);
            model.addAttribute("message", "Transfer completed successfully (ACID transaction secured)!");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "index";
    }

    // 5. Выписка по счету
    @GetMapping("/account/statement")
    public String statement(@RequestParam String accountNumber, Model model) {
        try {
            List<Transaction> history = bankService.getAccountStatement(accountNumber);
            model.addAttribute("transactions", history);
            model.addAttribute("targetAccount", accountNumber);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "index";
    }

    // 6. Аналитика
    @GetMapping("/analytics")
    public String analytics(@RequestParam Currency currency, Model model) {
        try {
            BigDecimal total = bankService.getTotalBalanceByCurrency(currency);
            long count = bankService.getTransactionCountByType(TransactionType.TRANSFER);
            model.addAttribute("analyticsResult", "Total balance in " + currency + ": " + total + " | Total transfers: " + count);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "index";
    }
}