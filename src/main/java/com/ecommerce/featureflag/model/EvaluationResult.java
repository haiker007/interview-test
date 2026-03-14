package com.ecommerce.featureflag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Result of flag evaluation with value, reason, and explanation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResult {
    private String flagKey;
    private Object value;
    private ReasonCode reason;
    private Map<String, Object> explanation;
    private List<String> trackEvents;
    private Instant evaluatedAt;
}
