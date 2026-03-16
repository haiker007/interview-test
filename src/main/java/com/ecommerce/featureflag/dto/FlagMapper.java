package com.ecommerce.featureflag.dto;

import com.ecommerce.featureflag.model.Condition;
import com.ecommerce.featureflag.model.Flag;
import com.ecommerce.featureflag.model.Rule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FlagMapper {
    FlagMapper INSTANCE = Mappers.getMapper(FlagMapper.class);

    FlagDto toDto(Flag flag);
    Flag toEntity(FlagDto dto);

    RuleDto toRuleDto(Rule rule);
    Rule toRuleEntity(RuleDto dto);

    ConditionDto toConditionDto(Condition condition);
    Condition toConditionEntity(ConditionDto dto);

    List<FlagDto> toDtoList(List<Flag> flags);
}
