package com.javos.serviceorder;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {
    @EntityGraph(attributePaths = {"client", "technician"})
    List<ServiceOrder> findAll();

    Optional<ServiceOrder> findByOrderNumber(String orderNumber);
    List<ServiceOrder> findByStatus(ServiceOrderStatus status);
    List<ServiceOrder> findByClientId(Long clientId);
    List<ServiceOrder> findByTechnicianId(Long technicianId);
}
