package com.ecommerce.featureflag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Rule for flag targeting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule {
    private String id;
    private String name;
    private RuleType type;
    private List<Condition> conditions;
    private String variation;
    private Integer rolloutPercentage;
    private List<String> targets;
    private Integer priority;

    public enum RuleType {
        GRADUAL_ROLLOUT,
        TARGET,
        DEFAULT
    }
}
