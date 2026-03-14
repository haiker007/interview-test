package com.ecommerce.featureflag.service;

import com.ecommerce.featureflag.model.EvaluationContext;
import com.ecommerce.featureflag.model.EvaluationResult;
import com.ecommerce.featureflag.model.Flag;
import com.ecommerce.featureflag.model.FlagEvaluator;
import com.ecommerce.featureflag.model.ReasonCode;
import com.ecommerce.featureflag.model.Rule;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ecommerce.featureflag.model.FlagEvaluatorUtils.buildExplanation;

/**
 * Default implementation of FlagEvaluator.
 * Uses RuleEvaluatorRegistry for extensible rule evaluation.
 */
@Component
public class DefaultFlagEvaluator implements FlagEvaluator {

    private final RuleEvaluatorRegistry ruleEvaluatorRegistry;
    private final ConditionEvaluatorRegistry conditionEvaluatorRegistry;

    public DefaultFlagEvaluator(RuleEvaluatorRegistry ruleEvaluatorRegistry,
                                ConditionEvaluatorRegistry conditionEvaluatorRegistry) {
        this.ruleEvaluatorRegistry = ruleEvaluatorRegistry;
        this.conditionEvaluatorRegistry = conditionEvaluatorRegistry;
    }

    @Override
    public EvaluationResult evaluate(Flag flag, EvaluationContext context) {
        // Check if disabled
        if (flag.getStatus() == Flag.FlagStatus.DISABLED) {
            Object defaultValue = flag.defaultValue();
            if (!isValidType(defaultValue, flag.getType())) {
                return typeMismatchResult(flag, defaultValue, "default");
            }
            return EvaluationResult.builder()
                    .flagKey(flag.getKey())
                    .value(defaultValue)
                    .reason(ReasonCode.DISABLED)
                    .evaluatedAt(Instant.now())
                    .build();
        }

        // Sort rules by priority
        List<Rule> sortedRules = flag.getRules() != null
            ? flag.getRules().stream()
                .sorted(Comparator.comparingInt(r -> r.getPriority() != null ? r.getPriority() : 0))
                .collect(Collectors.toList())
            : Collections.emptyList();

        // Evaluate each rule using the registry
        for (Rule rule : sortedRules) {
            Boolean ruleResult = ruleEvaluatorRegistry.evaluate(rule, flag, context);
            if (ruleResult == null) ruleResult = false;
            if (Boolean.TRUE.equals(ruleResult)) {
                Object variationValue = flag.getVariations().get(rule.getVariation());
                if (!isValidType(variationValue, flag.getType())) {
                    return typeMismatchResult(flag, variationValue, "rule");
                }
                return EvaluationResult.builder()
                        .flagKey(flag.getKey())
                        .value(variationValue)
                        .reason(ReasonCode.RULE_MATCH)
                        .trackEvents(flag.isTrackEvents() ? List.of(flag.getKey()) : Collections.emptyList())
                        .evaluatedAt(Instant.now())
                        .build();
            }
        }

        // Return default variation
        Object defaultValue = flag.defaultValue();
        if (!isValidType(defaultValue, flag.getType())) {
            return typeMismatchResult(flag, defaultValue, "default");
        }
        return EvaluationResult.builder()
                .flagKey(flag.getKey())
                .value(defaultValue)
                .reason(ReasonCode.DEFAULT)
                .evaluatedAt(Instant.now())
                .build();
    }

    @Override
    public List<EvaluationResult> evaluateAll(List<Flag> flags, EvaluationContext context) {
        return flags.stream()
                .map(flag -> evaluate(flag, context))
                .collect(Collectors.toList());
    }

    /**
     * Evaluate and always include explanation, even for default.
     */
    public EvaluationResult evaluateWithExplanation(Flag flag, EvaluationContext context) {
        EvaluationResult result = evaluate(flag, context);

        String explanationReason = switch (result.getReason()) {
            case DISABLED -> "Flag is disabled";
            case RULE_MATCH -> {
                Rule matchedRule = findMatchedRule(flag, context);
                yield matchedRule != null ? "Rule matched: " + matchedRule.getName() : "Rule matched";
            }
            case DEFAULT -> "Default value used";
            case TARGET_MATCH -> "Target match";
            case FALLTHROUGH -> "Fallthrough";
            case AFTER_ROLLOUT -> "After rollout";
            case ERROR -> {
                Map<String, Object> expl = result.getExplanation();
                yield expl != null && expl.get("reason") != null
                    ? (String) expl.get("reason")
                    : "Evaluation error";
            }
        };

        return EvaluationResult.builder()
                .flagKey(result.getFlagKey())
                .value(result.getValue())
                .reason(result.getReason())
                .explanation(Map.of("reason", explanationReason))
                .trackEvents(result.getTrackEvents())
                .evaluatedAt(result.getEvaluatedAt())
                .build();
    }

    private Rule findMatchedRule(Flag flag, EvaluationContext context) {
        if (flag.getRules() == null) return null;

        List<Rule> sortedRules = flag.getRules().stream()
                .sorted(Comparator.comparingInt(r -> r.getPriority() != null ? r.getPriority() : 0))
                .collect(Collectors.toList());

        for (Rule rule : sortedRules) {
            Boolean ruleResult = ruleEvaluatorRegistry.evaluate(rule, flag, context);
            if (Boolean.TRUE.equals(ruleResult)) {
                return rule;
            }
        }
        return null;
    }

    private boolean isValidType(Object value, Flag.FlagType type) {
        if (type == null) {
            return true;
        }
        if (value == null) {
            return false;
        }
        return switch (type) {
            case BOOLEAN -> value instanceof Boolean;
            case STRING -> value instanceof String;
            case NUMBER -> value instanceof Number;
            case JSON -> true;
        };
    }

    private EvaluationResult typeMismatchResult(Flag flag, Object value, String source) {
        return EvaluationResult.builder()
                .flagKey(flag.getKey())
                .value(null)
                .reason(ReasonCode.ERROR)
                .explanation(Map.of(
                        "reason", "TYPE_MISMATCH",
                        "expectedType", flag.getType() != null ? flag.getType().name() : "",
                        "actualType", value != null ? value.getClass().getSimpleName() : "null",
                        "source", source
                ))
                .evaluatedAt(Instant.now())
                .build();
    }
}
