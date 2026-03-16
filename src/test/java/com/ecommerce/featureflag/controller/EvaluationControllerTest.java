package com.ecommerce.featureflag.controller;

import com.ecommerce.featureflag.model.Flag;
import com.ecommerce.featureflag.service.FlagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for EvaluationController using real context.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FlagRepository flagRepository;

    @BeforeEach
    void setUp() {
        flagRepository.deleteAll();
        Flag boolFlag = Flag.builder()
                .key("boolean-flag")
                .name("Boolean Feature Flag")
                .type(Flag.FlagType.BOOLEAN)
                .status(Flag.FlagStatus.ENABLED)
                .variations(Map.of("true", true, "false", false))
                .defaultVariation("true")
                .trackEvents(true)
                .build();
        flagRepository.save(boolFlag);
    }

    @Test
    void evaluateGet_singleFlag_returnsEvaluationResult() throws Exception {
        mockMvc.perform(get("/api/v1/flags/boolean-flag/evaluate")
                        .param("userId", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flagKey").value("boolean-flag"))
                .andExpect(jsonPath("$.value").value(true));
    }

    @Test
    void evaluatePost_batchFlags_returnsMultipleResults() throws Exception {
        String requestBody = """
                {
                    "flags": ["boolean-flag"],
                    "context": {
                        "userId": "user-123",
                        "attributes": {
                            "tier": "premium"
                        }
                    }
                }
                """;

        mockMvc.perform(post("/api/v1/flags/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results[0].flagKey").value("boolean-flag"));
    }

    @Test
    void explain_getFlagExplanation_returnsDetailedInfo() throws Exception {
        mockMvc.perform(get("/api/v1/flags/boolean-flag/explain")
                        .param("userId", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flagKey").value("boolean-flag"))
                .andExpect(jsonPath("$.reason").exists())
                .andExpect(jsonPath("$.explanation").exists());
    }

    @Test
    void evaluate_unknownFlag_returnsErrorReason() throws Exception {
        mockMvc.perform(get("/api/v1/flags/unknown-flag/evaluate")
                        .param("userId", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flagKey").value("unknown-flag"))
                .andExpect(jsonPath("$.reason").value("ERROR"));
    }
}
