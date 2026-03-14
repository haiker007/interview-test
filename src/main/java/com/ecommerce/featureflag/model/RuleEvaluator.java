package com.ecommerce.featureflag.model;

/**
 * Strategy interface for evaluating different rule types.
 * Implement this interface to add custom rule evaluation logic.
 */
public interface RuleEvaluator {

    /**
     * Check if this evaluator can handle the given rule type.
     * @param ruleType the type of rule to evaluate
     * @return true if this evaluator can handle the rule type
     */
    boolean supports(Rule.RuleType ruleType);

    /**
     * Evaluate the rule against the evaluation context.
     * @param rule the rule to evaluate
     * @param flag the flag being evaluated
     * @param context the evaluation context
     * @return true if the rule matches, false otherwise
     */
    boolean evaluate(Rule rule, Flag flag, EvaluationContext context);
}
