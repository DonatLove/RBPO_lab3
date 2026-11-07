package org.example.services;

public class AccountAlreadyExistsException extends RuntimeException {
    public AccountAlreadyExistsException() {
        super("Имя пользователя уже занято");
    }
}
