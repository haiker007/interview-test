package com.ecommerce.featureflag.model;

/**
 * Strategy interface for evaluating a single condition/operator.
 */
public interface ConditionEvaluator {
    /**
     * Whether this evaluator supports the given operator.
     */
    boolean supports(Condition.Operator operator);

    /**
     * Evaluate the condition against an actual value from context.
     */
    boolean evaluate(Object actualValue, Object expectedValue, Condition condition);
}
