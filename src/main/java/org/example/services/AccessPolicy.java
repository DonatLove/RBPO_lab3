package org.example.services;

import org.example.models.ItemClaimStatus;
import org.example.repositories.ItemClaimRepository;
import org.example.repositories.LossReportRepository;
import org.example.repositories.UserRepository;
import org.springframework.stereotype.Component;

@Component("accessPolicy")
public class AccessPolicy {
    private final UserRepository userRepository;
    private final LossReportRepository lossReportRepository;
    private final ItemClaimRepository itemClaimRepository;

    public AccessPolicy(
            UserRepository userRepository,
            LossReportRepository lossReportRepository,
            ItemClaimRepository itemClaimRepository) {
        this.userRepository = userRepository;
        this.lossReportRepository = lossReportRepository;
        this.itemClaimRepository = itemClaimRepository;
    }

    public boolean canAccessUser(Long userId, String username) {
        return userRepository.existsByIdAndUsername(userId, username);
    }

    public boolean canAccessLossReport(Long reportId, String username) {
        return lossReportRepository.existsByIdAndApplicant_Username(reportId, username);
    }

    public boolean canAccessClaim(Long claimId, String username) {
        return itemClaimRepository.existsByIdAndLossReport_Applicant_Username(claimId, username);
    }

    public boolean canCancelPendingClaim(Long claimId, String username) {
        return itemClaimRepository.existsByIdAndStatusAndLossReport_Applicant_Username(
                claimId, ItemClaimStatus.PENDING, username);
    }
}
