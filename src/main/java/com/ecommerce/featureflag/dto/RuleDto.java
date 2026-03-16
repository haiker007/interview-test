package com.ecommerce.featureflag.dto;

import com.ecommerce.featureflag.model.Rule;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RuleDto {
    private String id;
    private String name;
    private Rule.RuleType type;
    private List<ConditionDto> conditions;
    private String variation;
    private Integer rolloutPercentage;
    private List<String> targets;
    private Integer priority;
}
