package com.ecommerce.featureflag.model.evaluator;

import com.ecommerce.featureflag.model.Condition;
import com.ecommerce.featureflag.model.EvaluationContext;
import com.ecommerce.featureflag.model.Flag;
import com.ecommerce.featureflag.model.Rule;
import com.ecommerce.featureflag.model.RuleEvaluator;
import com.ecommerce.featureflag.service.ConditionEvaluatorRegistry;
import org.springframework.stereotype.Component;

/**
 * Evaluator for TARGET rule type.
 * Evaluates rules based on user targeting and conditions.
 */
@Component
public class TargetRuleEvaluator implements RuleEvaluator {

    private final ConditionEvaluatorRegistry conditionEvaluatorRegistry;

    public TargetRuleEvaluator(ConditionEvaluatorRegistry conditionEvaluatorRegistry) {
        this.conditionEvaluatorRegistry = conditionEvaluatorRegistry;
    }

    @Override
    public boolean supports(Rule.RuleType ruleType) {
        return ruleType == Rule.RuleType.TARGET;
    }

    @Override
    public boolean evaluate(Rule rule, Flag flag, EvaluationContext context) {
        if (rule.getConditions() == null || rule.getConditions().isEmpty()) {
            return false;
        }

        // Check if user is directly targeted
        if (rule.getTargets() != null && rule.getTargets().contains(context.getUserId())) {
            return true;
        }

        // Evaluate all conditions (AND logic)
        for (Condition condition : rule.getConditions()) {
            if (!conditionEvaluatorRegistry.evaluate(condition, context)) {
                return false;
            }
        }
        return true;
    }
}
