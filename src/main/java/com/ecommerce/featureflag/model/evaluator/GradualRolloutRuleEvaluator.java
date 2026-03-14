package com.ecommerce.featureflag.model.evaluator;

import com.ecommerce.featureflag.model.EvaluationContext;
import com.ecommerce.featureflag.model.Flag;
import com.ecommerce.featureflag.model.Rule;
import com.ecommerce.featureflag.model.RuleEvaluator;
import org.springframework.stereotype.Component;

/**
 * Evaluator for GRADUAL_ROLLOUT rule type.
 * Uses consistent hashing to deterministically assign users to buckets.
 */
@Component
public class GradualRolloutRuleEvaluator implements RuleEvaluator {

    @Override
    public boolean supports(Rule.RuleType ruleType) {
        return ruleType == Rule.RuleType.GRADUAL_ROLLOUT;
    }

    @Override
    public boolean evaluate(Rule rule, Flag flag, EvaluationContext context) {
        if (rule.getRolloutPercentage() == null || rule.getRolloutPercentage() == 0) {
            return false;
        }

        // 100% rollout always returns true
        if (rule.getRolloutPercentage() == 100) {
            return true;
        }

        if (context.getUserId() == null) {
            return false;
        }

        // Use consistent hashing for bucket assignment
        int bucket = calculateBucket(flag.getKey(), context.getUserId());
        return bucket < rule.getRolloutPercentage();
    }

    /**
     * Calculate bucket using consistent hashing.
     */
    private int calculateBucket(String flagKey, String userId) {
        String bucketKey = flagKey + ":" + userId;
        int hash = Math.abs(bucketKey.hashCode());
        return hash % 100;
    }
}
