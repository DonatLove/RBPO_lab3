package org.example.services;

import org.example.factories.UserFactory;
import org.example.models.User;
import org.example.repositories.LossReportRepository;
import org.example.repositories.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final LossReportRepository lossReportRepository;
    private final UserFactory userFactory;

    public UserService(
            UserRepository userRepository,
            LossReportRepository lossReportRepository,
            UserFactory userFactory) {
        this.userRepository = userRepository;
        this.lossReportRepository = lossReportRepository;
        this.userFactory = userFactory;
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

    @Transactional
    public User create(String fullName, String contactDetails) {
        return userRepository.save(userFactory.create(
                requireText(fullName, "ФИО пользователя"),
                requireText(contactDetails, "Контактные данные")));
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
}
