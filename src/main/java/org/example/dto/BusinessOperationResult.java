package org.example.dto;

import org.example.models.FoundItemStatus;
import org.example.models.ItemClaimStatus;
import org.example.models.LossReportStatus;

import java.util.List;

public record BusinessOperationResult(
        String operation,
        Long claimId,
        ItemClaimStatus claimStatus,
        Long foundItemId,
        FoundItemStatus foundItemStatus,
        Long lossReportId,
        LossReportStatus lossReportStatus,
        Long handoverRecordId,
        List<Long> affectedClaimIds) {
}
