package com.ecommerce.featureflag.service;

import com.ecommerce.featureflag.model.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for FlagEvaluationService using real context.
 */
@SpringBootTest
@Transactional
class FlagEvaluatorTest {

    @Autowired
    private FlagRepository flagRepository;

    @Autowired
    private FlagEvaluationService flagEvaluationService;

    @Autowired
    private FlagStore flagStore;

    @BeforeEach
    void setUp() {
        flagRepository.deleteAll();
        // Initialize with sample flag for basic tests
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
    void evaluate_booleanFlag_returnsTrueWhenEnabled() {
        EvaluationContext context = EvaluationContext.builder()
                .userId("user-123")
                .build();

        EvaluationResult result = flagEvaluationService.evaluate("boolean-flag", context);

        assertNotNull(result);
        assertEquals("boolean-flag", result.getFlagKey());
        assertEquals(true, result.getValue());
    }

    @Test
    void evaluate_unknownFlag_returnsError() {
        EvaluationContext context = EvaluationContext.builder()
                .userId("user-123")
                .build();

        EvaluationResult result = flagEvaluationService.evaluate("unknown-flag", context);

        assertNotNull(result);
        assertEquals("unknown-flag", result.getFlagKey());
        assertNull(result.getValue());
        assertEquals(ReasonCode.ERROR, result.getReason());
    }

    @Test
    void evaluate_flagWithRule_matchedByUserAttribute() {
        Flag flag = Flag.builder()
                .key("premium-feature")
                .name("Premium Feature")
                .type(Flag.FlagType.BOOLEAN)
                .status(Flag.FlagStatus.ENABLED)
                .defaultVariation("false")
                .variations(Map.of("true", true, "false", false))
                .build();

        Rule rule = Rule.builder()
                .name("Premium Users")
                .type(Rule.RuleType.TARGET)
                .conditions(List.of(Condition.builder()
                        .attribute("tier")
                        .operator(Condition.Operator.EQUAL)
                        .value("premium")
                        .build()))
                .variation("true")
                .priority(1)
                .build();

        flag.addRule(rule);
        flagRepository.save(flag);

        EvaluationContext context = EvaluationContext.builder()
                .userId("user-123")
                .attributes(Map.of("tier", "premium"))
                .build();

        EvaluationResult result = flagEvaluationService.evaluate("premium-feature", context);

        assertNotNull(result);
        assertEquals(true, result.getValue());
        assertEquals(ReasonCode.RULE_MATCH, result.getReason());
    }

    @Test
    void evaluate_rolloutPercentage_matchedByHash() {
        Flag flag = Flag.builder()
                .key("rollout-feature")
                .name("Rollout Feature")
                .type(Flag.FlagType.BOOLEAN)
                .status(Flag.FlagStatus.ENABLED)
                .defaultVariation("false")
                .variations(Map.of("true", true, "false", false))
                .build();

        Rule rule = Rule.builder()
                .name("Gradual Rollout")
                .type(Rule.RuleType.GRADUAL_ROLLOUT)
                .rolloutPercentage(100)
                .variation("true")
                .priority(1)
                .build();

        flag.addRule(rule);
        flagRepository.save(flag);

        EvaluationContext context = EvaluationContext.builder()
                .userId("user-123")
                .build();

        EvaluationResult result = flagEvaluationService.evaluate("rollout-feature", context);

        assertNotNull(result);
        assertEquals(true, result.getValue());
    }

    @Test
    void evaluate_batchFlags_returnsAllResults() {
        Flag flagA = Flag.builder()
                .key("flag-a")
                .name("Flag A")
                .type(Flag.FlagType.BOOLEAN)
                .status(Flag.FlagStatus.ENABLED)
                .defaultVariation("false")
                .variations(Map.of("true", true, "false", false))
                .build();
        flagRepository.save(flagA);

        Flag flagB = Flag.builder()
                .key("flag-b")
                .name("Flag B")
                .type(Flag.FlagType.STRING)
                .status(Flag.FlagStatus.ENABLED)
                .defaultVariation("value-b")
                .variations(Map.of("value-b", "value-b"))
                .build();
        flagRepository.save(flagB);

        EvaluationContext context = EvaluationContext.builder()
                .userId("user-123")
                .build();

        List<EvaluationResult> results = flagEvaluationService.evaluateAll(
                List.of("flag-a", "flag-b"), context);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(r -> "flag-a".equals(r.getFlagKey())));
        assertTrue(results.stream().anyMatch(r -> "flag-b".equals(r.getFlagKey())));
    }

    @Test
    void evaluate_disabledFlag_returnsDisabledReason() {
        Flag flag = Flag.builder()
                .key("disabled-flag")
                .name("Disabled Flag")
                .type(Flag.FlagType.BOOLEAN)
                .status(Flag.FlagStatus.DISABLED)
                .defaultVariation("false")
                .variations(Map.of("true", true, "false", false))
                .build();
        flagRepository.save(flag);

        EvaluationContext context = EvaluationContext.builder()
                .userId("user-123")
                .build();

        EvaluationResult result = flagEvaluationService.evaluate("disabled-flag", context);

        assertNotNull(result);
        assertEquals(ReasonCode.DISABLED, result.getReason());
    }
}
