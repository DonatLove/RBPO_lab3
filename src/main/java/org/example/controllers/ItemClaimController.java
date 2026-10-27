package org.example.controllers;

import org.example.annotation.Cumponent;
import org.example.facades.ItemClaimFacade;
import org.example.models.ItemClaim;
import org.example.services.ItemClaimService;
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
@RequestMapping("/api/item-claims")
public class ItemClaimController {
    private final ItemClaimService itemClaimService;

    public ItemClaimController(ItemClaimService itemClaimService) {
        this.itemClaimService = itemClaimService;
    }

    @GetMapping
    public ResponseEntity<List<ItemClaim>> getAll() {
        return ResponseEntity.ok(itemClaimService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemClaim> getById(@PathVariable Long id) {
        return ResponseEntity.ok(itemClaimService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ItemClaim> create(
            @RequestParam Long lossReportId,
            @RequestParam Long foundItemId,
            @RequestBody ItemClaimFacade facade) {
        ItemClaim itemClaim = itemClaimService.create(
                lossReportId, foundItemId, facade.getEvidence(), facade.getStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(itemClaim);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemClaim> update(
            @PathVariable Long id,
            @RequestParam(required = false) Long lossReportId,
            @RequestParam(required = false) Long foundItemId,
            @RequestBody ItemClaimFacade facade) {
        return ResponseEntity.ok(itemClaimService.update(
                id, lossReportId, foundItemId, facade.getEvidence(), facade.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemClaimService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
