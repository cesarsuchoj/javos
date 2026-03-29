package com.javos.sale;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    @EntityGraph(attributePaths = {"client", "seller", "items", "items.product"})
    List<Sale> findAll();

    Optional<Sale> findBySaleNumber(String saleNumber);
    List<Sale> findByClientId(Long clientId);
    List<Sale> findByStatus(SaleStatus status);
}
