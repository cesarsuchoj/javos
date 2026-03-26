package com.javos.serviceorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OsAttachmentRepository extends JpaRepository<OsAttachment, Long> {
    List<OsAttachment> findByServiceOrderId(Long serviceOrderId);
}
