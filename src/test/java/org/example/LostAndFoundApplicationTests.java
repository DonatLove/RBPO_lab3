package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.models.User;
import org.example.models.UserRole;
import org.example.repositories.FoundItemRepository;
import org.example.repositories.HandoverRecordRepository;
import org.example.repositories.ItemClaimRepository;
import org.example.repositories.LossReportRepository;
import org.example.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LostAndFoundApplicationTests {
    private static final String USERNAME = "anna";
    private static final String USER_PASSWORD = "StrongPass#2026";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "AdminPass#2026";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private FoundItemRepository foundItemRepository;
    @Autowired private LossReportRepository lossReportRepository;
    @Autowired private ItemClaimRepository itemClaimRepository;
    @Autowired private HandoverRecordRepository handoverRecordRepository;

    @BeforeEach
    void prepareAccounts() throws Exception {
        handoverRecordRepository.deleteAll();
        itemClaimRepository.deleteAll();
        lossReportRepository.deleteAll();
        foundItemRepository.deleteAll();
        userRepository.deleteAll();

        User admin = new User();
        admin.setUsername(ADMIN_USERNAME);
        admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setAppRole(UserRole.ADMIN);
        admin.setFullName("Администратор");
        admin.setContactDetails("admin@example.com");
        userRepository.save(admin);

        registerAccount(USERNAME, USER_PASSWORD, "Анна Лебедева", "anna@example.com");
    }

    @Test
    void businessOperationsKeepRelatedEntitiesConsistentAndEnforceRoles() throws Exception {
        long foundOneId = createFoundItem("Чёрный зонт", "Аксессуары", "Аудитория 101");
        long reportOneId = createLossReport("Потерян чёрный зонт", "Аксессуары");
        long claimOneId = createClaim(reportOneId, foundOneId, "Фотография ручки");

        mockMvc.perform(post("/api/business/claims/{id}/approve", claimOneId).with(userRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Недостаточно прав для выполнения операции"));

        mockMvc.perform(post("/api/business/claims/{id}/approve", claimOneId).with(adminRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claimStatus").value("APPROVED"))
                .andExpect(jsonPath("$.foundItemStatus").value("RESERVED"))
                .andExpect(jsonPath("$.lossReportStatus").value("MATCHED"));

        mockMvc.perform(post("/api/business/claims/{id}/handover", claimOneId).with(adminRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claimStatus").value("HANDED_OVER"))
                .andExpect(jsonPath("$.foundItemStatus").value("HANDED_OVER"))
                .andExpect(jsonPath("$.lossReportStatus").value("CLOSED"))
                .andExpect(jsonPath("$.handoverRecordId").isNumber());

        long foundTwoId = createFoundItem("Серый шарф", "Аксессуары", "Библиотека");
        long reportTwoId = createLossReport("Потерян серый шарф", "Аксессуары");
        long claimTwoId = createClaim(reportTwoId, foundTwoId, "Описание узора");
        mockMvc.perform(post("/api/business/claims/{id}/approve", claimTwoId).with(adminRequest()))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/business/claims/{id}/cancel", claimTwoId).with(adminRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claimStatus").value("CANCELLED"))
                .andExpect(jsonPath("$.foundItemStatus").value("AVAILABLE"))
                .andExpect(jsonPath("$.lossReportStatus").value("OPEN"));

        long foundThreeId = createFoundItem("Красные перчатки", "Аксессуары", "Холл");
        long rejectedClaimId = createClaim(reportTwoId, foundThreeId, "Фотография перчаток");
        mockMvc.perform(post("/api/business/claims/{id}/reject", rejectedClaimId).with(adminRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claimStatus").value("REJECTED"));

        long foundFourId = createFoundItem("Кожаный ремень", "Аксессуары", "Гардероб");
        long pendingClaimId = createClaim(reportTwoId, foundFourId, "Описание пряжки");
        mockMvc.perform(post("/api/business/loss-reports/{id}/close-without-match", reportTwoId)
                        .with(adminRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lossReportStatus").value("CLOSED_UNMATCHED"))
                .andExpect(jsonPath("$.affectedClaimIds[0]").value(pendingClaimId));

        mockMvc.perform(get("/api/item-claims/{id}", pendingClaimId).with(userRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void registrationUsesBcryptRejectsWeakPasswordsAndNeverGrantsAdmin() throws Exception {
        MvcResult result = registerAccount("candidate", "StrongPass#2026", "Новый пользователь", "new@example.com");
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals("USER", body.get("role").asText());

        User registered = userRepository.findByUsername("candidate").orElseThrow();
        assertNotEquals("StrongPass#2026", registered.getPasswordHash());
        assertTrue(passwordEncoder.matches("StrongPass#2026", registered.getPasswordHash()));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"weak\",\"password\":\"weak\",\"fullName\":\"Н\",\"contactDetails\":\"x\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"candidate\",\"password\":\"OtherStrong#2026\",\"fullName\":\"Другой\",\"contactDetails\":\"other@example.com\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void csrfCookieAndHeaderAreRequiredForRegistration() throws Exception {
        String registrationBody = "{\"username\":\"csrfuser\",\"password\":\"StrongPass#2026\",\"fullName\":\"Проверка CSRF\",\"contactDetails\":\"csrf@example.com\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Отсутствует или недействителен CSRF-токен"));

        MvcResult tokenResult = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode tokenBody = objectMapper.readTree(tokenResult.getResponse().getContentAsString());
        MockHttpSession session = (MockHttpSession) tokenResult.getRequest().getSession(false);
        assertNotNull(session);

        mockMvc.perform(post("/api/auth/register")
                        .session(session)
                        .header(tokenBody.get("headerName").asText(), tokenBody.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void rolesAndOwnershipRestrictReadsAndWrites() throws Exception {
        long ownReportId = createLossReport("Мой зонт", "Аксессуары");
        long adminReportId = createLossReportAsAdmin("Отчёт администратора", "Электроника");

        mockMvc.perform(get("/api/found-items"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Требуется аутентификация"));
        mockMvc.perform(get("/api/loss-reports").with(userRequest()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/loss-reports/{id}", adminReportId).with(userRequest()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/found-items")
                        .with(userRequest())
                        .param("location", "Холл")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Зонт\",\"category\":\"Аксессуары\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/loss-reports/{id}", ownReportId).with(userRequest()))
                .andExpect(status().isOk());
        Long adminId = userRepository.findByUsername(ADMIN_USERNAME).orElseThrow().getId();
        mockMvc.perform(patch("/api/loss-reports/{id}", ownReportId)
                        .with(userRequest())
                        .param("applicantId", Long.toString(adminId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
        assertEquals(userRepository.findByUsername(USERNAME).orElseThrow().getId(),
                lossReportRepository.findById(ownReportId).orElseThrow().getApplicant().getId());
    }

    @Test
    void categoryChangesCannotInvalidateExistingClaims() throws Exception {
        long itemId = createFoundItem("Чёрный зонт", "Аксессуары", "Аудитория 101");
        long reportId = createLossReport("Потерянный зонт", "Аксессуары");
        createClaim(reportId, itemId, "Царапина на ручке");

        mockMvc.perform(patch("/api/found-items/{id}", itemId)
                        .with(adminRequest())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":\"Электроника\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/loss-reports/{id}", reportId)
                        .with(userRequest())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":\"Электроника\"}"))
                .andExpect(status().isBadRequest());

        assertEquals("Аксессуары", foundItemRepository.findById(itemId).orElseThrow().getCategory());
        assertEquals("Аксессуары", lossReportRepository.findById(reportId).orElseThrow().getCategory());
    }

    @Test
    void disabledAndLockedAccountsCannotAuthenticate() throws Exception {
        User user = userRepository.findByUsername(USERNAME).orElseThrow();
        user.setEnabled(false);
        user = userRepository.save(user);
        mockMvc.perform(get("/api/auth/me").with(userRequest()))
                .andExpect(status().isUnauthorized());

        user.setEnabled(true);
        user.setLocked(true);
        user = userRepository.save(user);
        mockMvc.perform(get("/api/auth/me").with(userRequest()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCanCancelOwnPendingClaimButCannotApproveIt() throws Exception {
        long itemId = createFoundItem("Синий шарф", "Аксессуары", "Библиотека");
        long reportId = createLossReport("Потерян синий шарф", "Аксессуары");
        long claimId = createClaim(reportId, itemId, "Описание узора");

        mockMvc.perform(post("/api/business/claims/{id}/approve", claimId).with(userRequest()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/business/claims/{id}/cancel", claimId).with(userRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claimStatus").value("CANCELLED"));
    }

    private MvcResult registerAccount(String username, String password, String fullName, String contact) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "password", password,
                "fullName", fullName,
                "contactDetails", contact,
                "role", "ADMIN"));
        return mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private long createFoundItem(String description, String category, String location) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/found-items")
                        .with(adminRequest())
                        .param("location", location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("description", description, "category", category))))
                .andExpect(status().isCreated())
                .andReturn();
        return idFrom(result);
    }

    private long createLossReport(String description, String category) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/loss-reports")
                        .with(userRequest())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("description", description, "category", category))))
                .andExpect(status().isCreated())
                .andReturn();
        return idFrom(result);
    }

    private long createLossReportAsAdmin(String description, String category) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/loss-reports")
                        .with(adminRequest())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("description", description, "category", category))))
                .andExpect(status().isCreated())
                .andReturn();
        return idFrom(result);
    }

    private long createClaim(long reportId, long itemId, String evidence) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/item-claims")
                        .with(userRequest())
                        .param("lossReportId", Long.toString(reportId))
                        .param("foundItemId", Long.toString(itemId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("evidence", evidence))))
                .andExpect(status().isCreated())
                .andReturn();
        return idFrom(result);
    }

    private long idFrom(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").longValue();
    }

    private RequestPostProcessor userRequest() {
        return request -> {
            request = httpBasic(USERNAME, USER_PASSWORD).postProcessRequest(request);
            return csrf().postProcessRequest(request);
        };
    }

    private RequestPostProcessor adminRequest() {
        return request -> {
            request = httpBasic(ADMIN_USERNAME, ADMIN_PASSWORD).postProcessRequest(request);
            return csrf().postProcessRequest(request);
        };
    }
}
