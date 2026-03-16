package com.ecommerce.featureflag.dto;

import com.ecommerce.featureflag.model.Condition;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConditionDto {
    private String attribute;
    private Condition.Operator operator;
    private Object value;
}
