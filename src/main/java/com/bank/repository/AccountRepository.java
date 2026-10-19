package com.bank.repository;

import com.bank.model.Account;
import com.bank.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // Spring сам создаст запрос по имени метода: SELECT * FROM accounts WHERE account_number = ?
    Optional<Account> findByAccountNumber(String accountNumber);

    // Проверка существования счета
    boolean existsByAccountNumber(String accountNumber);

    // Агрегация для аналитики (общий баланс по валюте)
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.currency = :currency")
    BigDecimal getTotalBalanceByCurrency(Currency currency);
}
