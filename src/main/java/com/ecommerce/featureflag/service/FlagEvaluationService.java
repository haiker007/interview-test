package com.ecommerce.featureflag.service;

import com.ecommerce.featureflag.model.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for evaluating feature flags.
 */
@Component
public class FlagEvaluationService {

    private final FlagStore flagStore;
    private final FlagEvaluator flagEvaluator;
    private final Counter evaluationErrorCounter;

    public FlagEvaluationService(FlagStore flagStore, FlagEvaluator flagEvaluator, MeterRegistry meterRegistry) {
        this.flagStore = flagStore;
        this.flagEvaluator = flagEvaluator;
        this.evaluationErrorCounter = meterRegistry.counter("flag.evaluation.errors");
    }

    /**
     * Evaluate a single flag.
     */
    public EvaluationResult evaluate(String flagKey, EvaluationContext context) {
        Optional<Flag> flagOpt = flagStore.getFlag(flagKey);

        if (flagOpt.isEmpty()) {
            return EvaluationResult.builder()
                    .flagKey(flagKey)
                    .value(null)
                    .reason(ReasonCode.ERROR)
                    .evaluatedAt(Instant.now())
                    .build();
        }

        Flag flag = flagOpt.get();
        try {
            return flagEvaluator.evaluate(flag, context);
        } catch (RuntimeException ex) {
            evaluationErrorCounter.increment();
            return EvaluationResult.builder()
                    .flagKey(flagKey)
                    .value(null)
                    .reason(ReasonCode.ERROR)
                    .explanation(Map.of("error", ex.getMessage()))
                    .evaluatedAt(Instant.now())
                    .build();
        }
    }

    /**
     * Evaluate a single flag, optionally forcing explanation.
     */
    public EvaluationResult evaluateWithExplanation(String flagKey, EvaluationContext context, boolean explain) {
        Optional<Flag> flagOpt = flagStore.getFlag(flagKey);

        if (flagOpt.isEmpty()) {
            return EvaluationResult.builder()
                    .flagKey(flagKey)
                    .value(null)
                    .reason(ReasonCode.ERROR)
                    .evaluatedAt(Instant.now())
                    .build();
        }

        Flag flag = flagOpt.get();
        try {
            if (explain && flagEvaluator instanceof com.ecommerce.featureflag.service.DefaultFlagEvaluator defeval) {
                return defeval.evaluateWithExplanation(flag, context);
            }
            return flagEvaluator.evaluate(flag, context);
        } catch (RuntimeException ex) {
            evaluationErrorCounter.increment();
            return EvaluationResult.builder()
                    .flagKey(flagKey)
                    .value(null)
                    .reason(ReasonCode.ERROR)
                    .explanation(Map.of("error", ex.getMessage()))
                    .evaluatedAt(Instant.now())
                    .build();
        }
    }

    /**
     * Evaluate multiple flags in batch.
     */
    public List<EvaluationResult> evaluateAll(List<String> flagKeys, EvaluationContext context) {
        // First pass: get all flags from store, preserving order
        List<Flag> foundFlags = new ArrayList<>();
        List<String> missingKeys = new ArrayList<>();

        for (String key : flagKeys) {
            Optional<Flag> flagOpt = flagStore.getFlag(key);
            if (flagOpt.isPresent()) {
                foundFlags.add(flagOpt.get());
            } else {
                missingKeys.add(key);
            }
        }

        List<EvaluationResult> results;

        try {
            // Evaluate found flags using evaluator
            results = foundFlags.isEmpty()
                ? new ArrayList<>()
                : flagEvaluator.evaluateAll(foundFlags, context);
        } catch (RuntimeException ex) {
            evaluationErrorCounter.increment();
            results = foundFlags.stream()
                .map(flag -> EvaluationResult.builder()
                    .flagKey(flag.getKey())
                    .value(null)
                    .reason(ReasonCode.ERROR)
                    .explanation(Map.of("error", ex.getMessage()))
                    .evaluatedAt(Instant.now())
                    .build())
                .toList();
        }

        // Build result map for quick lookup
        Map<String, EvaluationResult> resultMap = results.stream()
            .collect(Collectors.toMap(EvaluationResult::getFlagKey, r -> r));

        // Reconstruct results in original order, including missing flags
        List<EvaluationResult> finalResults = new ArrayList<>();

        for (String key : flagKeys) {
            if (missingKeys.contains(key)) {
                finalResults.add(EvaluationResult.builder()
                    .flagKey(key)
                    .value(null)
                    .reason(ReasonCode.ERROR)
                    .evaluatedAt(Instant.now())
                    .build());
            } else {
                finalResults.add(resultMap.get(key));
            }
        }

        return finalResults;
    }
}
