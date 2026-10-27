package org.example.controllers;

import org.example.annotation.Cumponent;
import org.example.facades.HandoverRecordFacade;
import org.example.models.HandoverRecord;
import org.example.services.HandoverRecordService;
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
@RequestMapping("/api/handover-records")
public class HandoverRecordController {
    private final HandoverRecordService handoverRecordService;

    public HandoverRecordController(HandoverRecordService handoverRecordService) {
        this.handoverRecordService = handoverRecordService;
    }

    @GetMapping
    public ResponseEntity<List<HandoverRecord>> getAll() {
        return ResponseEntity.ok(handoverRecordService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HandoverRecord> getById(@PathVariable Long id) {
        return ResponseEntity.ok(handoverRecordService.findById(id));
    }

    @PostMapping
    public ResponseEntity<HandoverRecord> create(
            @RequestParam Long itemClaimId,
            @RequestBody HandoverRecordFacade facade) {
        HandoverRecord handoverRecord = handoverRecordService.create(
                itemClaimId, facade.getConfirmed());
        return ResponseEntity.status(HttpStatus.CREATED).body(handoverRecord);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<HandoverRecord> update(
            @PathVariable Long id,
            @RequestParam(required = false) Long itemClaimId,
            @RequestBody HandoverRecordFacade facade) {
        return ResponseEntity.ok(handoverRecordService.update(
                id, itemClaimId, facade.getConfirmed()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        handoverRecordService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
