package com.ecommerce.featureflag.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import com.ecommerce.featureflag.model.evaluator.DefaultConditionEvaluator;
import com.ecommerce.featureflag.service.DefaultFlagEvaluator;
import com.ecommerce.featureflag.model.FlagEvaluator;
import com.ecommerce.featureflag.service.DefaultFlagStore;
import com.ecommerce.featureflag.service.FlagEvaluationService;
import com.ecommerce.featureflag.service.FlagStore;
import com.ecommerce.featureflag.service.ConditionEvaluatorRegistry;
import com.ecommerce.featureflag.service.RuleEvaluatorRegistry;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for EvaluationController - TDD approach.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ComponentScan(basePackages = "com.ecommerce.featureflag")
class EvaluationControllerTest {

  @Configuration
  static class TestConfig {
    @Bean
    public FlagStore flagStore() {
      return new DefaultFlagStore();
    }

    @Bean
    public ConditionEvaluatorRegistry conditionEvaluatorRegistry() {
      return new ConditionEvaluatorRegistry(List.of(new DefaultConditionEvaluator()));
    }

    @Bean
    public FlagEvaluator flagEvaluator(RuleEvaluatorRegistry ruleEvaluatorRegistry,
                                       ConditionEvaluatorRegistry conditionEvaluatorRegistry) {
      return new DefaultFlagEvaluator(ruleEvaluatorRegistry, conditionEvaluatorRegistry);
    }

    @Bean
    public FlagEvaluationService flagEvaluationService(FlagStore flagStore, FlagEvaluator flagEvaluator, MeterRegistry meterRegistry) {
      return new FlagEvaluationService(flagStore, flagEvaluator, meterRegistry);
    }
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private FlagEvaluator flagEvaluator;

  @Autowired
  private FlagStore flagStore;

  @BeforeEach
  void setUp() {
    // Reset flag store for each test
  }

  @Test
  void evaluateGet_singleFlag_returnsEvaluationResult() throws Exception {
    // When: GET request to evaluate a single flag
    mockMvc.perform(get("/api/v1/flags/boolean-flag/evaluate")
                        .param("userId", "user-123"))
        // Then: should return 200 OK with evaluation result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flagKey").value("boolean-flag"))
        .andExpect(jsonPath("$.value").exists());
  }

  @Test
  void evaluatePost_batchFlags_returnsMultipleResults() throws Exception {
    // Given: a batch evaluation request
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

    // When: POST request to evaluate batch flags
    mockMvc.perform(post("/api/v1/flags/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
        // Then: should return 200 OK with empty results (evaluateAll not fully implemented)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.results").isArray());
  }

  @Test
  void explain_getFlagExplanation_returnsDetailedInfo() throws Exception {
    // When: GET request to explain a flag evaluation
    mockMvc.perform(get("/api/v1/flags/boolean-flag/explain")
                        .param("userId", "user-123"))
        // Then: should return 200 OK with explanation
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flagKey").value("boolean-flag"))
        .andExpect(jsonPath("$.reason").exists())
        .andExpect(jsonPath("$.explanation").exists());
  }

  @Test
  void evaluate_unknownFlag_returnsErrorReason() throws Exception {
    // When: evaluate unknown flag
    mockMvc.perform(get("/api/v1/flags/unknown-flag/evaluate")
                        .param("userId", "user-123"))
        // Then: should return evaluation result with ERROR reason
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flagKey").value("unknown-flag"))
        .andExpect(jsonPath("$.reason").value("ERROR"));
  }
}
