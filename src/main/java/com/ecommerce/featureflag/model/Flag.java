package com.ecommerce.featureflag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Feature flag definition.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Flag {
    private String id;
    private String key;
    private String name;
    private FlagType type;
    private FlagStatus status;
    private Map<String, Object> variations;
    private String defaultVariation;
    private List<Rule> rules;
    private List<String> tags;
    private boolean trackEvents;

    public enum FlagType {
        BOOLEAN, STRING, NUMBER, JSON
    }

    public enum FlagStatus {
        ENABLED, DISABLED
    }

    public Object defaultValue() {
        if (this.defaultVariation != null && this.variations != null) {
            return variations.get(defaultVariation);
        }
        return null;
    }
}
