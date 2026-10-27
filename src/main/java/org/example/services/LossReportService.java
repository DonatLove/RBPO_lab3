package org.example.services;

import org.example.factories.LossReportFactory;
import org.example.models.LossReport;
import org.example.models.LossReportStatus;
import org.example.models.User;
import org.example.repositories.ItemClaimRepository;
import org.example.repositories.LossReportRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class LossReportService {
    private final LossReportRepository lossReportRepository;
    private final ItemClaimRepository itemClaimRepository;
    private final UserService userService;
    private final LossReportFactory lossReportFactory;

    public LossReportService(
            LossReportRepository lossReportRepository,
            ItemClaimRepository itemClaimRepository,
            UserService userService,
            LossReportFactory lossReportFactory) {
        this.lossReportRepository = lossReportRepository;
        this.itemClaimRepository = itemClaimRepository;
        this.userService = userService;
        this.lossReportFactory = lossReportFactory;
    }

    @Transactional(readOnly = true)
    public List<LossReport> findAll() {
        return lossReportRepository.findAll(Sort.by("id"));
    }

    @Transactional(readOnly = true)
    public LossReport findById(Long id) {
        return lossReportRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Заявление о пропаже с ID " + id + " не найдено"));
    }

    @Transactional
    public LossReport create(Long applicantId, String description, String category, String status) {
        requireInitialStatus(status, "Статус заявления");
        User applicant = userService.findById(applicantId);
        return lossReportRepository.save(lossReportFactory.create(
                applicant,
                requireText(description, "Описание заявления"),
                requireText(category, "Категория предмета"),
                LossReportStatus.OPEN.name()));
    }

    @Transactional
    public LossReport update(Long id, Long applicantId, String description, String category, String status) {
        LossReport lossReport = findById(id);
        requireInitialStatus(status, "Статус заявления");
        if (applicantId != null) {
            lossReport.setApplicant(userService.findById(applicantId));
        }
        if (description != null) {
            lossReport.setDescription(requireText(description, "Описание заявления"));
        }
        if (category != null) {
            lossReport.setCategory(requireText(category, "Категория предмета"));
        }
        return lossReportRepository.save(lossReport);
    }

    @Transactional
    public void deleteById(Long id) {
        LossReport lossReport = findById(id);
        if (itemClaimRepository.existsByLossReport_Id(id)) {
            throw new IllegalArgumentException("Нельзя удалить заявление, связанное с претензиями");
        }
        lossReportRepository.delete(lossReport);
    }

    private void requireInitialStatus(String value, String fieldName) {
        if (value != null && !value.isBlank() && !LossReportStatus.OPEN.name().equals(value)) {
            throw new IllegalArgumentException(fieldName + " изменяется только бизнес-операциями");
        }
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
        return value.trim();
    }
}
