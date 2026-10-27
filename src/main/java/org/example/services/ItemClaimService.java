package org.example.services;

import org.example.factories.ItemClaimFactory;
import org.example.models.FoundItem;
import org.example.models.FoundItemStatus;
import org.example.models.ItemClaim;
import org.example.models.ItemClaimStatus;
import org.example.models.LossReport;
import org.example.models.LossReportStatus;
import org.example.repositories.HandoverRecordRepository;
import org.example.repositories.ItemClaimRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ItemClaimService {
    private final ItemClaimRepository itemClaimRepository;
    private final HandoverRecordRepository handoverRecordRepository;
    private final LossReportService lossReportService;
    private final FoundItemService foundItemService;
    private final ItemClaimFactory itemClaimFactory;

    public ItemClaimService(
            ItemClaimRepository itemClaimRepository,
            HandoverRecordRepository handoverRecordRepository,
            LossReportService lossReportService,
            FoundItemService foundItemService,
            ItemClaimFactory itemClaimFactory) {
        this.itemClaimRepository = itemClaimRepository;
        this.handoverRecordRepository = handoverRecordRepository;
        this.lossReportService = lossReportService;
        this.foundItemService = foundItemService;
        this.itemClaimFactory = itemClaimFactory;
    }

    @Transactional(readOnly = true)
    public List<ItemClaim> findAll() {
        return itemClaimRepository.findAll(Sort.by("id"));
    }

    @Transactional(readOnly = true)
    public ItemClaim findById(Long id) {
        return itemClaimRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Претензия с ID " + id + " не найдена"));
    }

    @Transactional
    public ItemClaim create(Long lossReportId, Long foundItemId, String evidence, String status) {
        requirePendingStatus(status);
        LossReport lossReport = lossReportService.findById(lossReportId);
        FoundItem foundItem = foundItemService.findById(foundItemId);
        ensureClaimCanBeCreated(lossReport, foundItem);
        return itemClaimRepository.save(itemClaimFactory.create(
                lossReport,
                foundItem,
                requireText(evidence, "Доказательства"),
                ItemClaimStatus.PENDING.name()));
    }

    @Transactional
    public ItemClaim update(Long id, Long lossReportId, Long foundItemId, String evidence, String status) {
        ItemClaim itemClaim = findById(id);
        if (itemClaim.getStatus() != ItemClaimStatus.PENDING) {
            throw new IllegalArgumentException("Изменять можно только претензию в статусе PENDING");
        }
        requirePendingStatus(status);

        LossReport lossReport = lossReportId == null
                ? itemClaim.getLossReport() : lossReportService.findById(lossReportId);
        FoundItem foundItem = foundItemId == null
                ? itemClaim.getFoundItem() : foundItemService.findById(foundItemId);
        ensureClaimCanBeCreated(lossReport, foundItem);

        itemClaim.setLossReport(lossReport);
        itemClaim.setFoundItem(foundItem);
        if (evidence != null) {
            itemClaim.setEvidence(requireText(evidence, "Доказательства"));
        }
        return itemClaimRepository.save(itemClaim);
    }

    @Transactional
    public void deleteById(Long id) {
        ItemClaim itemClaim = findById(id);
        if (itemClaim.getStatus() != ItemClaimStatus.PENDING
                || handoverRecordRepository.existsByItemClaim_Id(id)) {
            throw new IllegalArgumentException("Удалять можно только претензию PENDING без акта передачи");
        }
        itemClaimRepository.delete(itemClaim);
    }

    private void ensureClaimCanBeCreated(LossReport lossReport, FoundItem foundItem) {
        if (lossReport.getStatus() != LossReportStatus.OPEN) {
            throw new IllegalArgumentException("Претензию можно создать только для открытого заявления");
        }
        if (foundItem.getStatus() != FoundItemStatus.AVAILABLE) {
            throw new IllegalArgumentException("Претензию можно создать только для доступного предмета");
        }
        if (!lossReport.getCategory().equalsIgnoreCase(foundItem.getCategory())) {
            throw new IllegalArgumentException("Категории заявления и найденного предмета должны совпадать");
        }
    }

    private void requirePendingStatus(String status) {
        if (status != null && !status.isBlank() && !ItemClaimStatus.PENDING.name().equals(status)) {
            throw new IllegalArgumentException("Статус претензии изменяется только бизнес-операциями");
        }
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " не могут быть пустыми");
        }
        return value.trim();
    }
}
