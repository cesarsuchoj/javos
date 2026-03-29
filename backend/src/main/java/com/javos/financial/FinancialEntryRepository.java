package com.javos.financial;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FinancialEntryRepository extends JpaRepository<FinancialEntry, Long> {
    @EntityGraph(attributePaths = {"category", "account"})
    List<FinancialEntry> findAll();

    List<FinancialEntry> findByType(EntryType type);
    List<FinancialEntry> findByPaid(boolean paid);
    List<FinancialEntry> findByCategoryId(Long categoryId);
    List<FinancialEntry> findByAccountId(Long accountId);
}
