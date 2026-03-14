package com.ecommerce.featureflag.model;

import java.util.List;

public interface FlagEvaluator {
  EvaluationResult evaluate(Flag flag, EvaluationContext context);
  List<EvaluationResult> evaluateAll(List<Flag> flags, EvaluationContext context);
}
