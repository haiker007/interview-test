package com.ecommerce.featureflag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Context for flag evaluation containing user and environment information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationContext {
    private String userId;
    private Map<String, String> attributes;
    private String sessionId;
    private String environment;
}
