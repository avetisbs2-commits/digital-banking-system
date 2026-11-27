package com.bank.support;

import com.bank.model.AccountEntity;
import com.bank.model.Currency;
import com.bank.model.TransactionEntity;
import com.bank.model.TransactionType;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryRepositories {

    private final Map<String, AccountEntity> accounts = new LinkedHashMap<>();
    private final List<TransactionEntity> transactions = new ArrayList<>();
    private long nextAccountId = 1;

    public AccountRepository accountRepository() {
        return (AccountRepository) Proxy.newProxyInstance(
                AccountRepository.class.getClassLoader(),
                new Class<?>[]{AccountRepository.class},
                this::invokeAccountRepository
        );
    }

    public TransactionRepository transactionRepository() {
        return (TransactionRepository) Proxy.newProxyInstance(
                TransactionRepository.class.getClassLoader(),
                new Class<?>[]{TransactionRepository.class},
                this::invokeTransactionRepository
        );
    }

    public List<TransactionEntity> transactions() {
        return List.copyOf(transactions);
    }

    private Object invokeAccountRepository(Object proxy, Method method, Object[] args) {
        if (method.getDeclaringClass() == Object.class) {
            return invokeObjectMethod(proxy, method, args);
        }

        return switch (method.getName()) {
            case "findByAccountNumber", "findByAccountNumberForPinVerification" ->
                    Optional.ofNullable(accounts.get((String) args[0]));
            case "existsByAccountNumber" -> accounts.containsKey((String) args[0]);
            case "save" -> saveAccount((AccountEntity) args[0]);
            case "findAll" -> new ArrayList<>(accounts.values());
            case "getTotalBalanceByCurrency" -> totalBalance((Currency) args[0]);
            default -> throw new UnsupportedOperationException(method.getName());
        };
    }

    private Object invokeTransactionRepository(Object proxy, Method method, Object[] args) {
        if (method.getDeclaringClass() == Object.class) {
            return invokeObjectMethod(proxy, method, args);
        }

        return switch (method.getName()) {
            case "save" -> {
                transactions.add((TransactionEntity) args[0]);
                yield args[0];
            }
            case "findByAccountNumber" -> transactions.stream()
                    .filter(tx -> args[0].equals(tx.getSenderAccountNumber())
                            || args[0].equals(tx.getReceiverAccountNumber()))
                    .toList();
            case "countByType" -> transactions.stream()
                    .filter(tx -> tx.getType() == args[0])
                    .count();
            default -> throw new UnsupportedOperationException(method.getName());
        };
    }

    private AccountEntity saveAccount(AccountEntity account) {
        if (account.getId() == null) {
            account.setId(nextAccountId++);
        }
        accounts.put(account.getAccountNumber(), account);
        return account;
    }

    private BigDecimal totalBalance(Currency currency) {
        return accounts.values().stream()
                .filter(account -> account.getCurrency() == currency)
                .map(AccountEntity::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Object invokeObjectMethod(Object proxy, Method method, Object[] args) {
        return switch (method.getName()) {
            case "toString" -> "InMemoryRepository";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> throw new UnsupportedOperationException(method.getName());
        };
    }
}