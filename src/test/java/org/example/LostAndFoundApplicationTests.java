package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LostAndFoundApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoundItemRepository foundItemRepository;

    @Autowired
    private LossReportRepository lossReportRepository;

    @Autowired
    private ItemClaimRepository itemClaimRepository;

    @Autowired
    private HandoverRecordRepository handoverRecordRepository;

    @BeforeEach
    void clearDatabase() {
        handoverRecordRepository.deleteAll();
        itemClaimRepository.deleteAll();
        lossReportRepository.deleteAll();
        foundItemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void businessOperationsKeepAllRelatedEntitiesConsistent() throws Exception {
        long userId = createUser();

        long foundOneId = createFoundItem("Чёрный зонт", "Аксессуары", "Аудитория 101");
        long reportOneId = createLossReport(userId, "Потерян чёрный зонт", "Аксессуары");
        long claimOneId = createClaim(reportOneId, foundOneId, "Фотография ручки");

        mockMvc.perform(post("/api/business/claims/{id}/approve", claimOneId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claimStatus").value("APPROVED"))
                .andExpect(jsonPath("$.foundItemStatus").value("RESERVED"))
                .andExpect(jsonPath("$.lossReportStatus").value("MATCHED"));

        mockMvc.perform(post("/api/business/claims/{id}/handover", claimOneId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claimStatus").value("HANDED_OVER"))
                .andExpect(jsonPath("$.foundItemStatus").value("HANDED_OVER"))
                .andExpect(jsonPath("$.lossReportStatus").value("CLOSED"))
                .andExpect(jsonPath("$.handoverRecordId").isNumber());

        long foundTwoId = createFoundItem("Серый шарф", "Аксессуары", "Библиотека");
        long reportTwoId = createLossReport(userId, "Потерян серый шарф", "Аксессуары");
        long claimTwoId = createClaim(reportTwoId, foundTwoId, "Описание узора");

        mockMvc.perform(post("/api/business/claims/{id}/approve", claimTwoId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/business/claims/{id}/cancel", claimTwoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claimStatus").value("CANCELLED"))
                .andExpect(jsonPath("$.foundItemStatus").value("AVAILABLE"))
                .andExpect(jsonPath("$.lossReportStatus").value("OPEN"));

        long foundThreeId = createFoundItem("Красные перчатки", "Аксессуары", "Холл");
        long rejectedClaimId = createClaim(reportTwoId, foundThreeId, "Фотография перчаток");
        mockMvc.perform(post("/api/business/claims/{id}/reject", rejectedClaimId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claimStatus").value("REJECTED"));

        long foundFourId = createFoundItem("Кожаный ремень", "Аксессуары", "Гардероб");
        long pendingClaimId = createClaim(reportTwoId, foundFourId, "Описание пряжки");
        mockMvc.perform(post("/api/business/loss-reports/{id}/close-without-match", reportTwoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lossReportStatus").value("CLOSED_UNMATCHED"))
                .andExpect(jsonPath("$.affectedClaimIds[0]").value(pendingClaimId));

        mockMvc.perform(get("/api/item-claims/{id}", pendingClaimId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void claimWithMismatchedCategoriesIsRejectedWithoutChangingState() throws Exception {
        long userId = createUser();
        long foundItemId = createFoundItem("Чёрный зонт", "Аксессуары", "Аудитория 101");
        long lossReportId = createLossReport(userId, "Потерян ноутбук", "Электроника");

        // The domain rule is enforced when a claim is created, before it can be approved.
        mockMvc.perform(post("/api/item-claims")
                        .param("lossReportId", Long.toString(lossReportId))
                        .param("foundItemId", Long.toString(foundItemId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"evidence\":\"Описание предмета\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Категории заявления и найденного предмета должны совпадать"));

        assertEquals(0, itemClaimRepository.count(), "Несовместимая претензия не должна сохраняться");
        mockMvc.perform(get("/api/found-items/{id}", foundItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
        mockMvc.perform(get("/api/loss-reports/{id}", lossReportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void staticPutEndpointReturnsHtml() throws Exception {
        mockMvc.perform(put("/api/static-html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Бюро находок")));
    }

    private long createUser() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Анна Иванова\",\"contactDetails\":\"anna@example.com\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return idFrom(result);
    }

    private long createFoundItem(String description, String category, String location) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/found-items")
                        .param("location", location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"" + description + "\",\"category\":\"" + category + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return idFrom(result);
    }

    private long createLossReport(long applicantId, String description, String category) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/loss-reports")
                        .param("applicantId", Long.toString(applicantId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"" + description + "\",\"category\":\"" + category + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return idFrom(result);
    }

    private long createClaim(long reportId, long foundItemId, String evidence) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/item-claims")
                        .param("lossReportId", Long.toString(reportId))
                        .param("foundItemId", Long.toString(foundItemId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"evidence\":\"" + evidence + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return idFrom(result);
    }

    private long idFrom(MvcResult result) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").longValue();
    }
}
