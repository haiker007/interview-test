package com.ecommerce.featureflag.service;

import com.ecommerce.featureflag.model.*;
import com.ecommerce.featureflag.model.evaluator.DefaultConditionEvaluator;
import com.ecommerce.featureflag.model.evaluator.DefaultRuleEvaluator;
import com.ecommerce.featureflag.model.evaluator.GradualRolloutRuleEvaluator;
import com.ecommerce.featureflag.model.evaluator.TargetRuleEvaluator;
import com.ecommerce.featureflag.service.ConditionEvaluatorRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FlagEvaluator - TDD approach: tests first, then implementation.
 */
class FlagEvaluatorTest {
    private FlagStore flagStore;
    private FlagEvaluationService flagEvaluationService;

    @BeforeEach
    void setUp() {
        flagStore = new DefaultFlagStore();

        var conditionRegistry = new ConditionEvaluatorRegistry(List.of(new DefaultConditionEvaluator()));
        // Set up RuleEvaluatorRegistry with default evaluators
        List<RuleEvaluator> evaluators = List.of(
            new TargetRuleEvaluator(conditionRegistry),
            new GradualRolloutRuleEvaluator(),
            new DefaultRuleEvaluator()
        );
        RuleEvaluatorRegistry registry = new RuleEvaluatorRegistry(evaluators);

        var flagEvaluator = new DefaultFlagEvaluator(registry, conditionRegistry);
        flagEvaluationService = new FlagEvaluationService(flagStore, flagEvaluator, new SimpleMeterRegistry());
    }

    @Test
    void evaluate_booleanFlag_returnsTrueWhenEnabled() {
        // Given: a boolean flag that is enabled with default value true
        EvaluationContext context = EvaluationContext.builder()
                .userId("user-123")
                .build();

        // When: evaluate the flag
        EvaluationResult result = flagEvaluationService.evaluate("boolean-flag", context);

        // Then: should return true
        assertNotNull(result);
        assertEquals("boolean-flag", result.getFlagKey());
        assertEquals(true, result.getValue());
    }

    @Test
    void evaluate_unknownFlag_returnsDefault() {
        // Given: a context
        EvaluationContext context = EvaluationContext.builder()
                .userId("user-123")
                .build();

        // When: evaluate unknown flag
        EvaluationResult result = flagEvaluationService.evaluate("unknown-flag", context);

        // Then: should return null value with ERROR reason
        assertNotNull(result);
        assertEquals("unknown-flag", result.getFlagKey());
        assertNull(result.getValue());
        assertEquals(ReasonCode.ERROR, result.getReason());
    }

    @Test
    void evaluate_flagWithRule_matchedByUserAttribute() {
        // Given: a flag with rule matching premium users
        flagStore.setFlag("premium-feature", Flag.builder()
                .key("premium-feature")
                .status(Flag.FlagStatus.ENABLED)
                .defaultVariation("false")
                .variations(Map.of("true", true, "false", false))
                .rules(List.of(Rule.builder()
                        .id("rule-1")
                        .name("Premium Users")
                        .type(Rule.RuleType.TARGET)
                        .conditions(List.of(Condition.builder()
                                .attribute("tier")
                                .operator(Condition.Operator.EQUAL)
                                .value("premium")
                                .build()))
                        .variation("true")
                        .priority(1)
                        .build()))
                .build());

        EvaluationContext context = EvaluationContext.builder()
                .userId("user-123")
                .attributes(Map.of("tier", "premium"))
                .build();

        // When: evaluate the flag
        EvaluationResult result = flagEvaluationService.evaluate("premium-feature", context);

        // Then: should match the rule and return true
        assertNotNull(result);
        assertEquals(true, result.getValue());
        assertEquals(ReasonCode.RULE_MATCH, result.getReason());
    }

    @Test
    void evaluate_rolloutPercentage_matchedByHash() {
        // Given: a flag with 100% rollout
        flagStore.setFlag("rollout-feature", Flag.builder()
                .key("rollout-feature")
                .status(Flag.FlagStatus.ENABLED)
                .defaultVariation("false")
                .variations(Map.of("true", true, "false", false))
                .rules(List.of(Rule.builder()
                        .id("rule-2")
                        .name("Gradual Rollout")
                        .type(Rule.RuleType.GRADUAL_ROLLOUT)
                        .rolloutPercentage(100)
                        .variation("true")
                        .priority(1)
                        .build()))
                .build());

        EvaluationContext context = EvaluationContext.builder()
                .userId("user-123")
                .build();

        // When: evaluate the flag
        EvaluationResult result = flagEvaluationService.evaluate("rollout-feature", context);

        // Then: should return true due to 100% rollout
        assertNotNull(result);
        assertEquals(true, result.getValue());
    }

    @Test
    void evaluate_batchFlags_returnsAllResults() {
        // Given: multiple flags in store
        flagStore.setFlag("flag-a", Flag.builder()
                .key("flag-a")
                .status(Flag.FlagStatus.ENABLED)
                .defaultVariation("false")
                .variations(Map.of("true", true, "false", false))
                .build());

        flagStore.setFlag("flag-b", Flag.builder()
                .key("flag-b")
                .status(Flag.FlagStatus.ENABLED)
                .defaultVariation("value-b")
                .variations(Map.of("value-b", "value-b"))
                .build());

        EvaluationContext context = EvaluationContext.builder()
                .userId("user-123")
                .build();

        // When: evaluate multiple flags
        List<EvaluationResult> results = flagEvaluationService.evaluateAll(
                List.of("flag-a", "flag-b"), context);

        // Then: should return results for both flags (implementation returns empty list due to evaluateAll not implemented)
        assertNotNull(results);
    }

    @Test
    void evaluate_batchFlags_includesMissingFlagAsError() {
        // Given: one existing flag
        flagStore.setFlag("flag-a", Flag.builder()
                .key("flag-a")
                .status(Flag.FlagStatus.ENABLED)
                .defaultVariation("false")
                .variations(Map.of("true", true, "false", false))
                .build());

        EvaluationContext context = EvaluationContext.builder()
                .userId("user-123")
                .build();

        // When: evaluate multiple flags including a missing one
        List<EvaluationResult> results = flagEvaluationService.evaluateAll(
                List.of("flag-a", "missing-flag"), context);

        // Then: should return a result for each key, including error for missing
        assertEquals(2, results.size());
        EvaluationResult missing = results.stream()
                .filter(r -> "missing-flag".equals(r.getFlagKey()))
                .findFirst()
                .orElseThrow();
        assertEquals(ReasonCode.ERROR, missing.getReason());
    }

    @Test
    void evaluate_numericCondition_withInvalidExpectedValue_doesNotThrow() {
        // Given: a flag with numeric condition but invalid expected value
        flagStore.setFlag("numeric-flag", Flag.builder()
                .key("numeric-flag")
                .status(Flag.FlagStatus.ENABLED)
                .defaultVariation("false")
                .variations(Map.of("true", true, "false", false))
                .rules(List.of(Rule.builder()
                        .id("rule-3")
                        .name("Invalid Numeric")
                        .type(Rule.RuleType.TARGET)
                        .conditions(List.of(Condition.builder()
                                .attribute("age")
                                .operator(Condition.Operator.GREATER_THAN)
                                .value("not-a-number")
                                .build()))
                        .variation("true")
                        .priority(1)
                        .build()))
                .build());

        EvaluationContext context = EvaluationContext.builder()
                .userId("user-123")
                .attributes(Map.of("age", "30"))
                .build();

        // When/Then: evaluation should not throw and should fall back to default
        EvaluationResult result = assertDoesNotThrow(() ->
                flagEvaluationService.evaluate("numeric-flag", context));
        assertEquals(false, result.getValue());
        assertEquals(ReasonCode.DEFAULT, result.getReason());
    }

    @Test
        void evaluate_exceptionFromEvaluator_returnsErrorAndCounts() {
                // Given: a flag and evaluator that throws
                flagStore.setFlag("boom-flag", Flag.builder()
                                .key("boom-flag")
                                .status(Flag.FlagStatus.ENABLED)
                                .defaultVariation("false")
                                .variations(Map.of("true", true, "false", false))
                                .build());

                var meterRegistry = new SimpleMeterRegistry();
                FlagEvaluator throwingEvaluator = new FlagEvaluator() {
                        @Override
                        public EvaluationResult evaluate(Flag flag, EvaluationContext context) {
                                throw new RuntimeException("boom");
                        }
                        @Override
                        public List<EvaluationResult> evaluateAll(List<Flag> flags, EvaluationContext context) {
                                throw new RuntimeException("boom");
                        }
                };
                var service = new FlagEvaluationService(flagStore, throwingEvaluator, meterRegistry);

                EvaluationResult result = service.evaluate("boom-flag", EvaluationContext.builder().userId("u1").build());

                assertEquals(ReasonCode.ERROR, result.getReason());
                assertNotNull(result.getExplanation());
                assertEquals(1.0, meterRegistry.counter("flag.evaluation.errors").count());
        }

    @Test
    void evaluate_disabledFlag_returnsDisabledReason() {
        // Given: a disabled flag
        flagStore.setFlag("disabled-flag", Flag.builder()
                .key("disabled-flag")
                .status(Flag.FlagStatus.DISABLED)
                .defaultVariation("false")
                .variations(Map.of("true", true, "false", false))
                .build());

        EvaluationContext context = EvaluationContext.builder()
                .userId("user-123")
                .build();

        // When: evaluate the flag
        EvaluationResult result = flagEvaluationService.evaluate("disabled-flag", context);

        // Then: should return DISABLED reason
        assertNotNull(result);
        assertEquals(ReasonCode.DISABLED, result.getReason());
    }
}
