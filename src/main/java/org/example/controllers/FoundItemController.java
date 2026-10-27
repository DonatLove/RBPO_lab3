package org.example.controllers;

import org.example.annotation.Cumponent;
import org.example.facades.FoundItemFacade;
import org.example.models.FoundItem;
import org.example.services.FoundItemService;
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
@RequestMapping("/api/found-items")
public class FoundItemController {
    private final FoundItemService foundItemService;

    public FoundItemController(FoundItemService foundItemService) {
        this.foundItemService = foundItemService;
    }

    @GetMapping
    public ResponseEntity<List<FoundItem>> getAll() {
        return ResponseEntity.ok(foundItemService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoundItem> getById(@PathVariable Long id) {
        return ResponseEntity.ok(foundItemService.findById(id));
    }

    @PostMapping
    public ResponseEntity<FoundItem> create(
            @RequestParam String location,
            @RequestParam(required = false) String status,
            @RequestBody FoundItemFacade facade) {
        FoundItem foundItem = foundItemService.create(
                facade.getDescription(), facade.getCategory(), location, status);
        return ResponseEntity.status(HttpStatus.CREATED).body(foundItem);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FoundItem> update(
            @PathVariable Long id,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String status,
            @RequestBody FoundItemFacade facade) {
        return ResponseEntity.ok(foundItemService.update(
                id, facade.getDescription(), facade.getCategory(), location, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        foundItemService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
