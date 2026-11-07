package org.example.services;

import org.example.factories.UserFactory;
import org.example.models.User;
import org.example.models.UserRole;
import org.example.dto.RegistrationRequest;
import org.example.repositories.LossReportRepository;
import org.example.repositories.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final LossReportRepository lossReportRepository;
    private final UserFactory userFactory;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            LossReportRepository lossReportRepository,
            UserFactory userFactory,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.lossReportRepository = lossReportRepository;
        this.userFactory = userFactory;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll(Sort.by("id"));
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + id + " не найден"));
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(normalizeUsername(username))
                .orElseThrow(() -> new NoSuchElementException("Учетная запись не найдена"));
    }

    @Transactional
    public User create(String username, String password, String fullName, String contactDetails) {
        return createAccount(new RegistrationRequest(username, password, fullName, contactDetails));
    }

    @Transactional
    public User register(RegistrationRequest request) {
        return createAccount(request);
    }

    private User createAccount(RegistrationRequest request) {
        String username = normalizeUsername(request.username());
        if (!username.matches("[a-z0-9._-]{3,32}")) {
            throw new IllegalArgumentException("Имя пользователя должно содержать от 3 до 32 символов: латинские буквы, цифры, точку, дефис или подчеркивание");
        }
        if (userRepository.existsByUsername(username)) {
            throw new AccountAlreadyExistsException();
        }
        validatePassword(request.password());
        User user = userFactory.create(
                requireText(request.fullName(), "ФИО пользователя"),
                requireText(request.contactDetails(), "Контактные данные"));
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setAppRole(UserRole.USER);
        user.setEnabled(true);
        user.setLocked(false);
        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, String fullName, String contactDetails) {
        User user = findById(id);
        if (fullName != null) {
            user.setFullName(requireText(fullName, "ФИО пользователя"));
        }
        if (contactDetails != null) {
            user.setContactDetails(requireText(contactDetails, "Контактные данные"));
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteById(Long id) {
        User user = findById(id);
        if (lossReportRepository.existsByApplicant_Id(id)) {
            throw new IllegalArgumentException("Нельзя удалить пользователя с заявлениями о пропаже");
        }
        userRepository.delete(user);
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
        return value.trim();
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Имя пользователя обязательно");
        }
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 12
                || password.chars().noneMatch(Character::isUpperCase)
                || password.chars().noneMatch(Character::isLowerCase)
                || password.chars().noneMatch(Character::isDigit)
                || password.chars().allMatch(Character::isLetterOrDigit)) {
            throw new IllegalArgumentException("Пароль должен содержать не менее 12 символов, заглавную и строчную буквы, цифру и специальный символ");
        }
    }
}
