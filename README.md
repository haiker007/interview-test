# Feature Toggle Service

## 1. API Endpoints

### 1.1 Single Flag Evaluation (GET)

Evaluate a single feature flag:

```bash
curl "http://localhost:8080/api/v1/flags/boolean-flag/evaluate?userId=user-123"
```

With additional context:

```bash
curl "http://localhost:8080/api/v1/flags/new-feature/evaluate?userId=user-123&sessionId=sess-456&environment=prod"
```

**Response:**

```json
{
  "flagKey": "boolean-flag",
  "value": true,
  "reason": "DEFAULT",
  "evaluatedAt": "2026-03-14T10:30:00Z"
}
```

### 1.2 Batch Flag Evaluation (POST)

Evaluate multiple flags at once:

```bash
curl -X POST "http://localhost:8080/api/v1/flags/evaluate" \
  -H "Content-Type: application/json" \
  -d '{
    "flags": ["boolean-flag", "premium-feature"],
    "context": {
      "userId": "user-123",
      "sessionId": "sess-456",
      "environment": "prod",
      "attributes": {
        "tier": "premium",
        "country": "US"
      }
    }
  }'
```

**Response:**

```json
{
  "results": [
    {
      "flagKey": "boolean-flag",
      "value": true,
      "reason": "DEFAULT",
      "evaluatedAt": "2026-03-14T10:30:00Z"
    }
  ]
}
```

### 1.3 Explain Endpoint (GET)

Get detailed evaluation explanation with debug info:

```bash
curl "http://localhost:8080/api/v1/flags/boolean-flag/explain?userId=user-123"
```

**Response:**

```json
{
  "flagKey": "boolean-flag",
  "value": true,
  "reason": "DEFAULT",
  "explanation": {},
  "evaluatedAt": "2026-03-14T10:30:00Z"
}
```

### 1.4 Available Sample Flags

The service includes these pre-configured flags:

| Flag Key          | Description          | Rules                          |
| ----------------- | -------------------- | ------------------------------ |
| `boolean-flag`    | Basic boolean flag   | Default: true                  |
| `premium-feature` | TARGET rule          | Matches when `tier == premium` |
| `rollout-feature` | GRADUAL_ROLLOUT rule | 100% rollout                   |
| `disabled-flag`   | Disabled flag        | Always returns DISABLED reason |

### 1.5 Reason Codes

- `DEFAULT` - Flag evaluated to default value (no rules matched)
- `RULE_MATCH` - A rule matched and returned its variation
- `DISABLED` - Flag is disabled
- `ERROR` - Flag not found or other error

## 2. How to add custom rules

To add a new rule type, simply:

1. Create a new class implementing RuleEvaluator:

```java
@Component
public class MyCustomRuleEvaluator implements RuleEvaluator {
    @Override
    public boolean supports(Rule.RuleType ruleType) {
        return ruleType == Rule.RuleType.MY_CUSTOM;
    }

    @Override
    public boolean evaluate(Rule rule, Flag flag, EvaluationContext context) {
        // Your custom logic here
    }
}
```

2. Add new enum value to Rule.RuleType

The evaluator will be automatically registered via constructor injection and ready to use!
