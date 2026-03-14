package com.ecommerce.featureflag.model;

/**
 * Reason code for flag evaluation results.
 */
public enum ReasonCode {
    DEFAULT,
    RULE_MATCH,
    TARGET_MATCH,
    FALLTHROUGH,
    DISABLED,
    ERROR,
    AFTER_ROLLOUT
}
