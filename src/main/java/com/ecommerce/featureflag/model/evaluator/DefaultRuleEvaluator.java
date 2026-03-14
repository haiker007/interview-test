package com.ecommerce.featureflag.model.evaluator;

import com.ecommerce.featureflag.model.EvaluationContext;
import com.ecommerce.featureflag.model.Flag;
import com.ecommerce.featureflag.model.Rule;
import com.ecommerce.featureflag.model.RuleEvaluator;
import org.springframework.stereotype.Component;

/**
 * Evaluator for DEFAULT rule type.
 * Default rules always match (used as fallback).
 */
@Component
public class DefaultRuleEvaluator implements RuleEvaluator {

    @Override
    public boolean supports(Rule.RuleType ruleType) {
        return ruleType == Rule.RuleType.DEFAULT;
    }

    @Override
    public boolean evaluate(Rule rule, Flag flag, EvaluationContext context) {
        // Default rule always matches
        return true;
    }
}
