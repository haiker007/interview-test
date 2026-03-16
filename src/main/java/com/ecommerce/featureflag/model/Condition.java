package com.ecommerce.featureflag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Condition for rule evaluation (Value Object stored as JSON).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Condition implements Serializable {
    private String attribute;
    private Operator operator;
    private Object value;

    public enum Operator {
        EQUAL,
        NOT_EQUAL,
        IN,
        NOT_IN,
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH,
        LESS_THAN,
        GREATER_THAN,
        LESS_THAN_OR_EQUAL,
        GREATER_THAN_OR_EQUAL
    }
}
