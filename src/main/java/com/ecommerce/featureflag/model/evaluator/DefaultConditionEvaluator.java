package com.ecommerce.featureflag.model.evaluator;

import com.ecommerce.featureflag.model.Condition;
import com.ecommerce.featureflag.model.ConditionEvaluator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Default evaluator covering built-in operators.
 */
@Component
public class DefaultConditionEvaluator implements ConditionEvaluator {

    @Override
    public boolean supports(Condition.Operator operator) {
        return operator != null;
    }

    @Override
    public boolean evaluate(@NonNull Object actualValue, @NonNull Object expectedValue, Condition condition) {
        return switch (condition.getOperator()) {
            case EQUAL -> actualValue.toString().equals(expectedValue.toString());
            case NOT_EQUAL -> !actualValue.toString().equals(expectedValue.toString());
            case IN -> expectedValue instanceof Collection<?> expectedSet
                    && expectedSet.stream().anyMatch(v -> v.toString().equals(actualValue.toString()));
            case NOT_IN -> expectedValue instanceof Collection<?> expectedSet
                    && expectedSet.stream().noneMatch(v -> v.toString().equals(actualValue.toString()));
            case CONTAINS -> actualValue.toString().contains(expectedValue.toString());
            case STARTS_WITH -> actualValue.toString().startsWith(expectedValue.toString());
            case ENDS_WITH -> actualValue.toString().endsWith(expectedValue.toString());
            case LESS_THAN -> {
                Integer cmp = compareNumericSafe(actualValue, expectedValue);
                yield cmp != null && cmp < 0;
            }
            case GREATER_THAN -> {
                Integer cmp = compareNumericSafe(actualValue, expectedValue);
                yield cmp != null && cmp > 0;
            }
            case LESS_THAN_OR_EQUAL -> {
                Integer cmp = compareNumericSafe(actualValue, expectedValue);
                yield cmp != null && cmp <= 0;
            }
            case GREATER_THAN_OR_EQUAL -> {
                Integer cmp = compareNumericSafe(actualValue, expectedValue);
                yield cmp != null && cmp >= 0;
            }
        };
    }

    private Integer compareNumericSafe(Object a, Object b) {
        try {
            return compareNumeric(a, b);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private int compareNumeric(Object a, Object b) {
        double numA = toDouble(a);
        double numB = toDouble(b);
        return Double.compare(numA, numB);
    }

    private double toDouble(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(value.toString());
    }
}
