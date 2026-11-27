package com.bank.repository;

import com.bank.model.AccountEntity;
import com.bank.model.Currency;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    Optional<AccountEntity> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountEntity a WHERE a.accountNumber = :accountNumber")
    Optional<AccountEntity> findByAccountNumberForPinVerification(
            @Param("accountNumber") String accountNumber
    );

    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT SUM(a.balance) FROM AccountEntity a WHERE a.currency = :currency")
    BigDecimal getTotalBalanceByCurrency(Currency currency);
}