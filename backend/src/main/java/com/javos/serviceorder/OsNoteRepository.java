package com.javos.serviceorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OsNoteRepository extends JpaRepository<OsNote, Long> {
    List<OsNote> findByServiceOrderIdOrderByCreatedAtAsc(Long serviceOrderId);
}
