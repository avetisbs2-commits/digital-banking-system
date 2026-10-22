package com.bank.service;

import com.bank.exception.AccountNotFoundException;
import com.bank.exception.BankTransactionException;
import com.bank.exception.BankingException;
import com.bank.exception.InsufficientFundsException;
import com.bank.model.*;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

@Service
public class BankService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public BankService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public AccountEntity createAccount(String ownerName, String pinCode, BigDecimal initialBalance, Currency currency) {
        validatePin(pinCode);
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BankTransactionException("The balance cannot be negative.");
        }
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new BankingException("Owner name cannot be empty.");
        }
        if (currency == null) {
            throw new BankingException("Currency must be specified.");
        }

        String accountNumber = generateUniqueAccountNumber();
        AccountEntity account = new AccountEntity(accountNumber, ownerName, pinCode, initialBalance, currency);
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public AccountEntity getAccountDetails(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account " + accountNumber + " not found."));
    }


    @Transactional
    public void deposit(String accountNumber, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankTransactionException("The amount must be greater than zero.");
        }

        AccountEntity account = getAccountDetails(accountNumber);

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        TransactionEntity transaction = new TransactionEntity(null, accountNumber, amount, TransactionType.DEPOSIT);
        transactionRepository.save(transaction);
    }

    @Transactional
    public void withdraw(String accountNumber, String pinCode, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankTransactionException("The withdrawal amount must be greater than zero.");
        }

        AccountEntity account = getAccountDetails(accountNumber);
        validatePinCode(account, pinCode);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds. Current balance:" + account.getBalance());
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        TransactionEntity transaction = new TransactionEntity(accountNumber, null, amount, TransactionType.WITHDRAW);
        transactionRepository.save(transaction);
    }

    @Transactional
    public void transfer(String senderAccNum, String receiverAccNum, String pinCode, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankTransactionException("The transfer amount must be greater than zero.");
        }

        if (senderAccNum.equals(receiverAccNum)) {
            throw new BankTransactionException("The sender's account and the receiver's account cannot be the same.");
        }

        AccountEntity sender = getAccountDetails(senderAccNum);
        AccountEntity receiver = getAccountDetails(receiverAccNum);

        validatePinCode(sender, pinCode);

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("The sender has insufficient funds for the transfer.");
        }

        BigDecimal convertedAmount = convertCurrency(amount, sender.getCurrency(), receiver.getCurrency());

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(convertedAmount));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        TransactionEntity transaction = new TransactionEntity(senderAccNum, receiverAccNum, amount, TransactionType.TRANSFER);
        transactionRepository.save(transaction);
    }

    public BigDecimal convertCurrency(BigDecimal amount, Currency from, Currency to) {
        if (from == to) {
            return amount;
        }

        BigDecimal rateFromToAmd = getExchangeRateToAmd(from);
        BigDecimal rateToToAmd = getExchangeRateToAmd(to);

        BigDecimal amountInAmd = amount.multiply(rateFromToAmd);
        return amountInAmd.divide(rateToToAmd, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getExchangeRateToAmd(Currency currency) {
        return switch (currency) {
            case AMD -> BigDecimal.ONE;
            case USD -> new BigDecimal("388.50");
            case EUR -> new BigDecimal("420.00");
        };
    }

    @Transactional(readOnly = true)
    public List<TransactionEntity> getAccountStatement(String accountNumber) {
        getAccountDetails(accountNumber);
        return transactionRepository.findByAccountNumber(accountNumber);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalBalanceByCurrency(Currency currency) {
        BigDecimal total = accountRepository.getTotalBalanceByCurrency(currency);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public long getTransactionCountByType(TransactionType type) {
        return transactionRepository.countByType(type);
    }

    private void validatePin(String pinCode) {
        if (pinCode == null || !pinCode.matches("^\\d{4}$")) {
            throw new BankTransactionException("The PIN code must consist of exactly 4 digits.");
        }
    }

    private void validatePinCode(AccountEntity account, String pinCode) {
        if (!account.getPinCode().equals(pinCode)) {
            throw new BankTransactionException("Incorrect PIN code.");
        }
    }

    private String generateUniqueAccountNumber() {
        Random random = new Random();
        String accountNumber;
        do {
            StringBuilder sb = new StringBuilder("AM");
            for (int i = 0; i < 10; i++) {
                sb.append(random.nextInt(10));
            }
            accountNumber = sb.toString();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }
}