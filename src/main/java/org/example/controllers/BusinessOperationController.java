package org.example.controllers;

import org.example.dto.BusinessOperationResult;
import org.example.services.BusinessOperationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/business")
public class BusinessOperationController {
    private final BusinessOperationService businessOperationService;

    public BusinessOperationController(BusinessOperationService businessOperationService) {
        this.businessOperationService = businessOperationService;
    }

    @PostMapping("/claims/{claimId}/approve")
    public ResponseEntity<BusinessOperationResult> approveClaim(@PathVariable Long claimId) {
        return ResponseEntity.ok(businessOperationService.approveClaim(claimId));
    }

    @PostMapping("/claims/{claimId}/reject")
    public ResponseEntity<BusinessOperationResult> rejectClaim(@PathVariable Long claimId) {
        return ResponseEntity.ok(businessOperationService.rejectClaim(claimId));
    }

    @PostMapping("/claims/{claimId}/cancel")
    public ResponseEntity<BusinessOperationResult> cancelClaim(@PathVariable Long claimId) {
        return ResponseEntity.ok(businessOperationService.cancelClaim(claimId));
    }

    @PostMapping("/claims/{claimId}/handover")
    public ResponseEntity<BusinessOperationResult> handoverItem(@PathVariable Long claimId) {
        return ResponseEntity.ok(businessOperationService.handoverItem(claimId));
    }

    @PostMapping("/loss-reports/{lossReportId}/close-without-match")
    public ResponseEntity<BusinessOperationResult> closeWithoutMatch(@PathVariable Long lossReportId) {
        return ResponseEntity.ok(businessOperationService.closeWithoutMatch(lossReportId));
    }
}
