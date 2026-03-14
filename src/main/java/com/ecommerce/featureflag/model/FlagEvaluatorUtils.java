package com.ecommerce.featureflag.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlagEvaluatorUtils {
  public static Object getAttributeValue(EvaluationContext context, String attribute) {
    if (attribute == null) {
      return null;
    }

    // Handle nested attributes like "user.tier"
    if (attribute.contains(".")) {
      String[] parts = attribute.split("\\.");
      if (parts.length == 2 && "user".equals(parts[0])) {
        if (context.getAttributes() != null) {
          return context.getAttributes().get(parts[1]);
        }
      }
    }

    // Direct attributes
    if (context.getAttributes() != null) {
      return context.getAttributes().get(attribute);
    }
    return null;
  }

  /**
   * Build explanation for evaluation result.
   */
  public static Map<String, Object> buildExplanation(Rule rule, EvaluationContext context,
      com.ecommerce.featureflag.service.ConditionEvaluatorRegistry conditionEvaluatorRegistry) {
    Map<String, Object> explanation = new HashMap<>();
    explanation.put("rule", Map.of(
        "id", rule.getId() != null ? rule.getId() : "",
        "name", rule.getName() != null ? rule.getName() : "",
        "type", rule.getType() != null ? rule.getType().name() : ""
                                  ));

    if (rule.getConditions() != null) {
      List<Map<String, Object>> conditionResults = rule.getConditions().stream()
          .map(c -> {
            Object actualValue = getAttributeValue(context, c.getAttribute());
            boolean matched = conditionEvaluatorRegistry.evaluate(c, context);
            return Map.of(
                "attribute", c.getAttribute() != null ? c.getAttribute() : "",
                "operator", c.getOperator() != null ? c.getOperator().name() : "",
                "expectedValue", c.getValue() != null ? c.getValue() : "",
                "actualValue", actualValue != null ? actualValue : "",
                "matched", matched
                         );
          })
          .collect(Collectors.toList());
      explanation.put("evaluatedConditions", conditionResults);
    }
    return explanation;
  }
}
