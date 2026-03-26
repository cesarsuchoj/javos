package com.javos.charge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, Long> {
    List<Charge> findByClientId(Long clientId);
    List<Charge> findByStatus(ChargeStatus status);
    List<Charge> findByReferenceIdAndReferenceType(Long referenceId, String referenceType);
}
