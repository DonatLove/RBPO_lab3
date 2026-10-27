package org.example.repositories;

import jakarta.persistence.LockModeType;
import org.example.models.ItemClaim;
import org.example.models.ItemClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemClaimRepository extends JpaRepository<ItemClaim, Long> {
    boolean existsByFoundItem_Id(Long foundItemId);

    boolean existsByLossReport_Id(Long lossReportId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select claim from ItemClaim claim where claim.id = :id")
    Optional<ItemClaim> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select claim from ItemClaim claim where claim.lossReport.id = :reportId and claim.status = :status")
    List<ItemClaim> findAllByReportAndStatusForUpdate(
            @Param("reportId") Long reportId,
            @Param("status") ItemClaimStatus status);
}
