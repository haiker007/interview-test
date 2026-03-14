package com.ecommerce.featureflag.service;

import com.ecommerce.featureflag.model.Condition;
import com.ecommerce.featureflag.model.ConditionEvaluator;
import com.ecommerce.featureflag.model.EvaluationContext;
import com.ecommerce.featureflag.model.FlagEvaluatorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Registry for condition evaluators to allow extensible operators and attribute handling.
 */
@Component
public class ConditionEvaluatorRegistry {

    private final List<ConditionEvaluator> evaluators = new ArrayList<>();

    @Autowired
    public ConditionEvaluatorRegistry(List<ConditionEvaluator> evaluators) {
        evaluators.forEach(this::register);
    }

    public void register(ConditionEvaluator evaluator) {
        evaluators.add(evaluator);
    }

    public Optional<ConditionEvaluator> findEvaluator(Condition.Operator operator) {
        return evaluators.stream()
                .filter(e -> e.supports(operator))
                .findFirst();
    }

    public boolean evaluate(Condition condition, EvaluationContext context) {
        if (condition == null || condition.getOperator() == null) {
            return false;
        }

        Object actual = FlagEvaluatorUtils.getAttributeValue(context, condition.getAttribute());
        if (actual == null) {
            return false;
        }

        Object expected = condition.getValue();
        if (expected == null) {
            return false;
        }

        return findEvaluator(condition.getOperator())
                .map(evaluator -> evaluator.evaluate(actual, expected, condition))
                .orElse(false);
    }

    public List<ConditionEvaluator> getEvaluators() {
        return List.copyOf(evaluators);
    }
}
