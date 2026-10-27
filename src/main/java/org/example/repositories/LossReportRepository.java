package org.example.repositories;

import org.example.models.LossReport;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LossReportRepository extends JpaRepository<LossReport, Long> {
    boolean existsByApplicant_Id(Long applicantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select report from LossReport report where report.id = :id")
    Optional<LossReport> findByIdForUpdate(@Param("id") Long id);
}
