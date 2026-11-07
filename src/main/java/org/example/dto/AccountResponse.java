package org.example.dto;

import org.example.models.UserRole;

public record AccountResponse(
        Long id,
        String username,
        String fullName,
        UserRole role,
        boolean enabled,
        boolean locked) {
}
