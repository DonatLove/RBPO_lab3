package org.example.services;

import org.example.factories.FoundItemFactory;
import org.example.models.FoundItem;
import org.example.models.FoundItemStatus;
import org.example.repositories.FoundItemRepository;
import org.example.repositories.ItemClaimRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class FoundItemService {
    private final FoundItemRepository foundItemRepository;
    private final ItemClaimRepository itemClaimRepository;
    private final FoundItemFactory foundItemFactory;

    public FoundItemService(
            FoundItemRepository foundItemRepository,
            ItemClaimRepository itemClaimRepository,
            FoundItemFactory foundItemFactory) {
        this.foundItemRepository = foundItemRepository;
        this.itemClaimRepository = itemClaimRepository;
        this.foundItemFactory = foundItemFactory;
    }

    @Transactional(readOnly = true)
    public List<FoundItem> findAll() {
        return foundItemRepository.findAll(Sort.by("id"));
    }

    @Transactional(readOnly = true)
    public FoundItem findById(Long id) {
        return foundItemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Найденный предмет с ID " + id + " не найден"));
    }

    @Transactional
    public FoundItem create(String description, String category, String location, String status) {
        requireInitialStatus(status, "Статус найденного предмета");
        return foundItemRepository.save(foundItemFactory.create(
                requireText(description, "Описание предмета"),
                requireText(category, "Категория предмета"),
                requireText(location, "Место обнаружения"),
                FoundItemStatus.AVAILABLE.name()));
    }

    @Transactional
    public FoundItem update(Long id, String description, String category, String location, String status) {
        FoundItem foundItem = findById(id);
        requireInitialStatus(status, "Статус найденного предмета");
        if (description != null) {
            foundItem.setDescription(requireText(description, "Описание предмета"));
        }
        if (category != null) {
            foundItem.setCategory(requireText(category, "Категория предмета"));
        }
        if (location != null) {
            foundItem.setLocation(requireText(location, "Место обнаружения"));
        }
        return foundItemRepository.save(foundItem);
    }

    @Transactional
    public void deleteById(Long id) {
        FoundItem foundItem = findById(id);
        if (itemClaimRepository.existsByFoundItem_Id(id)) {
            throw new IllegalArgumentException("Нельзя удалить найденный предмет, связанный с претензиями");
        }
        foundItemRepository.delete(foundItem);
    }

    private void requireInitialStatus(String value, String fieldName) {
        if (value != null && !value.isBlank() && !FoundItemStatus.AVAILABLE.name().equals(value)) {
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
