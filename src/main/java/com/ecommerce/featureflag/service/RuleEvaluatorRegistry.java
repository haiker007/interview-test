package com.ecommerce.featureflag.service;

import com.ecommerce.featureflag.model.Rule;
import com.ecommerce.featureflag.model.RuleEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Registry for managing RuleEvaluator instances.
 * Supports registration of custom rule evaluators for extensibility.
 */
@Component
public class RuleEvaluatorRegistry {

    private final List<RuleEvaluator> evaluators = new ArrayList<>();

    /**
     * Register a rule evaluator.
     * @param evaluator the evaluator to register
     */
    public void register(RuleEvaluator evaluator) {
        evaluators.add(evaluator);
    }

    /**
     * Auto-register all RuleEvaluator beans using constructor injection.
     * @param evaluators list of all RuleEvaluator implementations
     */
    @Autowired
    public RuleEvaluatorRegistry(List<RuleEvaluator> evaluators) {
        evaluators.forEach(this::register);
    }

    /**
     * Find an evaluator that supports the given rule type.
     * @param ruleType the rule type to find evaluator for
     * @return Optional containing the evaluator if found
     */
    public Optional<RuleEvaluator> findEvaluator(Rule.RuleType ruleType) {
        return evaluators.stream()
                .filter(e -> e.supports(ruleType))
                .findFirst();
    }

    /**
     * Evaluate a rule using the appropriate evaluator.
     * @param rule the rule to evaluate
     * @param flag the flag being evaluated
     * @param context the evaluation context
     * @return true if the rule matches, false if no evaluator found or rule doesn't match
     */
    public Boolean evaluate(Rule rule, com.ecommerce.featureflag.model.Flag flag, com.ecommerce.featureflag.model.EvaluationContext context) {
        return findEvaluator(rule.getType())
                .map(evaluator -> evaluator.evaluate(rule, flag, context))
                .orElse(null);
    }

    /**
     * Get all registered evaluators.
     * @return list of all registered evaluators
     */
    public List<RuleEvaluator> getEvaluators() {
        return List.copyOf(evaluators);
    }
}
