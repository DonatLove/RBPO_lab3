package org.example.services;

import org.example.dto.BusinessOperationResult;
import org.example.factories.HandoverRecordFactory;
import org.example.models.FoundItem;
import org.example.models.FoundItemStatus;
import org.example.models.HandoverRecord;
import org.example.models.ItemClaim;
import org.example.models.ItemClaimStatus;
import org.example.models.LossReport;
import org.example.models.LossReportStatus;
import org.example.repositories.FoundItemRepository;
import org.example.repositories.HandoverRecordRepository;
import org.example.repositories.ItemClaimRepository;
import org.example.repositories.LossReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BusinessOperationService {
    private final ItemClaimRepository itemClaimRepository;
    private final FoundItemRepository foundItemRepository;
    private final LossReportRepository lossReportRepository;
    private final HandoverRecordRepository handoverRecordRepository;
    private final HandoverRecordFactory handoverRecordFactory;

    public BusinessOperationService(
            ItemClaimRepository itemClaimRepository,
            FoundItemRepository foundItemRepository,
            LossReportRepository lossReportRepository,
            HandoverRecordRepository handoverRecordRepository,
            HandoverRecordFactory handoverRecordFactory) {
        this.itemClaimRepository = itemClaimRepository;
        this.foundItemRepository = foundItemRepository;
        this.lossReportRepository = lossReportRepository;
        this.handoverRecordRepository = handoverRecordRepository;
        this.handoverRecordFactory = handoverRecordFactory;
    }

    @Transactional
    public BusinessOperationResult approveClaim(Long claimId) {
        ItemClaim claim = getClaimForUpdate(claimId);
        FoundItem foundItem = getFoundItemForUpdate(claim);
        LossReport lossReport = getLossReportForUpdate(claim);

        require(claim.getStatus() == ItemClaimStatus.PENDING,
                "Одобрить можно только претензию в статусе PENDING");
        require(foundItem.getStatus() == FoundItemStatus.AVAILABLE,
                "Предмет уже зарезервирован или передан");
        require(lossReport.getStatus() == LossReportStatus.OPEN,
                "Заявление уже не находится в статусе OPEN");
        require(lossReport.getCategory().equalsIgnoreCase(foundItem.getCategory()),
                "Категории заявления и найденного предмета должны совпадать");

        claim.setStatus(ItemClaimStatus.APPROVED);
        foundItem.setStatus(FoundItemStatus.RESERVED);
        lossReport.setStatus(LossReportStatus.MATCHED);

        return result("approve-claim", claim, foundItem, lossReport, null, List.of(claim.getId()));
    }

    @Transactional
    public BusinessOperationResult rejectClaim(Long claimId) {
        ItemClaim claim = getClaimForUpdate(claimId);
        require(claim.getStatus() == ItemClaimStatus.PENDING,
                "Отклонить можно только претензию в статусе PENDING");
        claim.setStatus(ItemClaimStatus.REJECTED);
        return result("reject-claim", claim, claim.getFoundItem(), claim.getLossReport(), null, List.of(claim.getId()));
    }

    @Transactional
    public BusinessOperationResult cancelClaim(Long claimId) {
        ItemClaim claim = getClaimForUpdate(claimId);
        FoundItem foundItem = getFoundItemForUpdate(claim);
        LossReport lossReport = getLossReportForUpdate(claim);

        require(claim.getStatus() == ItemClaimStatus.PENDING || claim.getStatus() == ItemClaimStatus.APPROVED,
                "Отменить можно только претензию PENDING или APPROVED");
        if (claim.getStatus() == ItemClaimStatus.APPROVED) {
            require(foundItem.getStatus() == FoundItemStatus.RESERVED,
                    "У одобренной претензии предмет должен быть зарезервирован");
            require(lossReport.getStatus() == LossReportStatus.MATCHED,
                    "У одобренной претензии заявление должно быть сопоставлено");
            foundItem.setStatus(FoundItemStatus.AVAILABLE);
            lossReport.setStatus(LossReportStatus.OPEN);
        }
        claim.setStatus(ItemClaimStatus.CANCELLED);
        return result("cancel-claim", claim, foundItem, lossReport, null, List.of(claim.getId()));
    }

    @Transactional
    public BusinessOperationResult handoverItem(Long claimId) {
        ItemClaim claim = getClaimForUpdate(claimId);
        FoundItem foundItem = getFoundItemForUpdate(claim);
        LossReport lossReport = getLossReportForUpdate(claim);

        require(claim.getStatus() == ItemClaimStatus.APPROVED,
                "Передать можно только предмет по одобренной претензии");
        require(foundItem.getStatus() == FoundItemStatus.RESERVED,
                "Передаваемый предмет должен быть зарезервирован");
        require(lossReport.getStatus() == LossReportStatus.MATCHED,
                "Заявление должно быть сопоставлено с предметом");

        HandoverRecord handoverRecord = handoverRecordRepository.findByItemClaim_Id(claimId)
                .orElseGet(() -> handoverRecordFactory.create(claim, false));
        require(!handoverRecord.isConfirmed(), "Передача по этой претензии уже подтверждена");
        handoverRecord.setConfirmed(true);
        handoverRecord.setHandoverTime(LocalDateTime.now());
        handoverRecord = handoverRecordRepository.save(handoverRecord);

        claim.setStatus(ItemClaimStatus.HANDED_OVER);
        foundItem.setStatus(FoundItemStatus.HANDED_OVER);
        lossReport.setStatus(LossReportStatus.CLOSED);

        return result("handover-item", claim, foundItem, lossReport, handoverRecord.getId(), List.of(claim.getId()));
    }

    @Transactional
    public BusinessOperationResult closeWithoutMatch(Long lossReportId) {
        LossReport lossReport = lossReportRepository.findByIdForUpdate(lossReportId)
                .orElseThrow(() -> new NoSuchElementException("Заявление о пропаже с ID " + lossReportId + " не найдено"));
        require(lossReport.getStatus() == LossReportStatus.OPEN,
                "Закрыть без совпадения можно только заявление в статусе OPEN");

        List<ItemClaim> pendingClaims = itemClaimRepository.findAllByReportAndStatusForUpdate(
                lossReportId, ItemClaimStatus.PENDING);
        require(!pendingClaims.isEmpty(),
                "Для закрытия без совпадения у заявления должна быть хотя бы одна претензия PENDING");
        pendingClaims.forEach(claim -> claim.setStatus(ItemClaimStatus.REJECTED));
        lossReport.setStatus(LossReportStatus.CLOSED_UNMATCHED);
        List<Long> affectedClaimIds = pendingClaims.stream().map(ItemClaim::getId).toList();

        return new BusinessOperationResult(
                "close-without-match", null, null, null, null,
                lossReport.getId(), lossReport.getStatus(), null, affectedClaimIds);
    }

    private ItemClaim getClaimForUpdate(Long id) {
        return itemClaimRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Претензия с ID " + id + " не найдена"));
    }

    private FoundItem getFoundItemForUpdate(ItemClaim claim) {
        return foundItemRepository.findByIdForUpdate(claim.getFoundItem().getId())
                .orElseThrow(() -> new NoSuchElementException("Найденный предмет претензии не найден"));
    }

    private LossReport getLossReportForUpdate(ItemClaim claim) {
        return lossReportRepository.findByIdForUpdate(claim.getLossReport().getId())
                .orElseThrow(() -> new NoSuchElementException("Заявление претензии не найдено"));
    }

    private BusinessOperationResult result(
            String operation,
            ItemClaim claim,
            FoundItem foundItem,
            LossReport lossReport,
            Long handoverRecordId,
            List<Long> affectedClaimIds) {
        return new BusinessOperationResult(
                operation,
                claim.getId(),
                claim.getStatus(),
                foundItem.getId(),
                foundItem.getStatus(),
                lossReport.getId(),
                lossReport.getStatus(),
                handoverRecordId,
                affectedClaimIds);
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
