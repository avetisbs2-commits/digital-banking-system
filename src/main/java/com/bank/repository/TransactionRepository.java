package com.bank.repository;

import com.bank.model.Transaction;
import com.bank.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Получение истории транзакций по номеру счета (как отправитель или получатель)
    @Query("SELECT t FROM Transaction t WHERE t.senderAccountNumber = :accNum OR t.receiverAccountNumber = :accNum ORDER BY t.timestamp DESC")
    List<Transaction> findByAccountNumber(@Param("accNum") String accountNumber);

    // Подсчет количества транзакций определенного типа (Spring сам сгенерирует COUNT-запрос)
    long countByType(TransactionType type);
}