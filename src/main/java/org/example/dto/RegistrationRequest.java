package org.example.dto;

public record RegistrationRequest(
        String username,
        String password,
        String fullName,
        String contactDetails) {
}
