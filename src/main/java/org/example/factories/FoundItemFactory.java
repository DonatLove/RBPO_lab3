package org.example.factories;

import org.example.annotation.Cumponent;
import org.example.models.FoundItem;

@Cumponent
public class FoundItemFactory {
    public FoundItem create(
            String description,
            String category,
            String location,
            String status) {
        FoundItem foundItem = new FoundItem();
        foundItem.setDescription(description);
        foundItem.setCategory(category);
        foundItem.setLocation(location);
        foundItem.setStatus(status);
        return foundItem;
    }
}
