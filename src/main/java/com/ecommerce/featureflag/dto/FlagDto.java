package com.ecommerce.featureflag.dto;

import com.ecommerce.featureflag.model.Condition;
import com.ecommerce.featureflag.model.Flag;
import com.ecommerce.featureflag.model.Rule;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class FlagDto {
    private String id;
    private String key;
    private String name;
    private Flag.FlagType type;
    private Flag.FlagStatus status;
    private Map<String, Object> variations;
    private String defaultVariation;
    private List<RuleDto> rules;
    private List<String> tags;
    private boolean trackEvents;
    private Instant createdAt;
    private Instant updatedAt;
}
