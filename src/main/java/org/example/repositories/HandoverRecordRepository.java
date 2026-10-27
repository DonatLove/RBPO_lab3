package org.example.repositories;

import org.example.models.HandoverRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HandoverRecordRepository extends JpaRepository<HandoverRecord, Long> {
    Optional<HandoverRecord> findByItemClaim_Id(Long itemClaimId);

    boolean existsByItemClaim_Id(Long itemClaimId);
}
