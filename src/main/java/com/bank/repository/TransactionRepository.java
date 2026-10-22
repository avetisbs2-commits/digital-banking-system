package com.bank.repository;

import com.bank.model.TransactionEntity;
import com.bank.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    @Query("SELECT t FROM TransactionEntity t WHERE t.senderAccountNumber = :accNum OR t.receiverAccountNumber = :accNum ORDER BY t.timestamp DESC")

    List<TransactionEntity> findByAccountNumber(@Param("accNum") String accountNumber);

    long countByType(TransactionType type);
}