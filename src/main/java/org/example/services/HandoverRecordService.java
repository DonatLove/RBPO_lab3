package org.example.services;

import org.example.factories.HandoverRecordFactory;
import org.example.models.HandoverRecord;
import org.example.models.ItemClaim;
import org.example.models.ItemClaimStatus;
import org.example.repositories.HandoverRecordRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class HandoverRecordService {
    private final HandoverRecordRepository handoverRecordRepository;
    private final ItemClaimService itemClaimService;
    private final HandoverRecordFactory handoverRecordFactory;

    public HandoverRecordService(
            HandoverRecordRepository handoverRecordRepository,
            ItemClaimService itemClaimService,
            HandoverRecordFactory handoverRecordFactory) {
        this.handoverRecordRepository = handoverRecordRepository;
        this.itemClaimService = itemClaimService;
        this.handoverRecordFactory = handoverRecordFactory;
    }

    @Transactional(readOnly = true)
    public List<HandoverRecord> findAll() {
        return handoverRecordRepository.findAll(Sort.by("id"));
    }

    @Transactional(readOnly = true)
    public HandoverRecord findById(Long id) {
        return handoverRecordRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Акт передачи с ID " + id + " не найден"));
    }

    @Transactional
    public HandoverRecord create(Long itemClaimId, Boolean confirmed) {
        if (Boolean.TRUE.equals(confirmed)) {
            throw new IllegalArgumentException("Подтверждение передачи выполняется бизнес-операцией handover");
        }
        ItemClaim itemClaim = itemClaimService.findById(itemClaimId);
        ensureDraftCanBeCreated(itemClaim);
        HandoverRecord handoverRecord = handoverRecordFactory.create(itemClaim, false);
        handoverRecord.setHandoverTime(LocalDateTime.now());
        return handoverRecordRepository.save(handoverRecord);
    }

    @Transactional
    public HandoverRecord update(Long id, Long itemClaimId, Boolean confirmed) {
        HandoverRecord handoverRecord = findById(id);
        if (handoverRecord.isConfirmed() || Boolean.TRUE.equals(confirmed)) {
            throw new IllegalArgumentException("Подтверждение передачи выполняется бизнес-операцией handover");
        }
        if (itemClaimId != null && !itemClaimId.equals(handoverRecord.getItemClaim().getId())) {
            ItemClaim itemClaim = itemClaimService.findById(itemClaimId);
            ensureDraftCanBeCreated(itemClaim);
            handoverRecord.setItemClaim(itemClaim);
        }
        return handoverRecordRepository.save(handoverRecord);
    }

    @Transactional
    public void deleteById(Long id) {
        HandoverRecord handoverRecord = findById(id);
        if (handoverRecord.isConfirmed()) {
            throw new IllegalArgumentException("Нельзя удалить подтверждённый акт передачи");
        }
        handoverRecordRepository.delete(handoverRecord);
    }

    private void ensureDraftCanBeCreated(ItemClaim itemClaim) {
        if (itemClaim.getStatus() != ItemClaimStatus.APPROVED) {
            throw new IllegalArgumentException("Черновик акта создаётся только для одобренной претензии");
        }
        if (handoverRecordRepository.existsByItemClaim_Id(itemClaim.getId())) {
            throw new IllegalArgumentException("Для претензии уже существует акт передачи");
        }
    }
}
