package org.example.controllers;

import org.example.annotation.Cumponent;
import org.example.facades.LossReportFacade;
import org.example.models.LossReport;
import org.example.services.LossReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Cumponent
@RestController
@RequestMapping("/api/loss-reports")
public class LossReportController {
    private final LossReportService lossReportService;

    public LossReportController(LossReportService lossReportService) {
        this.lossReportService = lossReportService;
    }

    @GetMapping
    public ResponseEntity<List<LossReport>> getAll() {
        return ResponseEntity.ok(lossReportService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LossReport> getById(@PathVariable Long id) {
        return ResponseEntity.ok(lossReportService.findById(id));
    }

    @PostMapping
    public ResponseEntity<LossReport> create(
            @RequestParam Long applicantId,
            @RequestParam(required = false) String status,
            @RequestBody LossReportFacade facade) {
        LossReport lossReport = lossReportService.create(
                applicantId, facade.getDescription(), facade.getCategory(), status);
        return ResponseEntity.status(HttpStatus.CREATED).body(lossReport);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LossReport> update(
            @PathVariable Long id,
            @RequestParam(required = false) Long applicantId,
            @RequestParam(required = false) String status,
            @RequestBody LossReportFacade facade) {
        return ResponseEntity.ok(lossReportService.update(
                id, applicantId, facade.getDescription(), facade.getCategory(), status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        lossReportService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
