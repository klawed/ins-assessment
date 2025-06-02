package com.insurance.policy.mapper;

import com.insurance.policy.entity.PolicyEntity;
import com.insurance.shared.dto.PolicyDto;
import com.insurance.shared.dto.PremiumScheduleDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PolicyMapper {

    // Map PolicyEntity to PolicyDto
    PolicyDto toDto(PolicyEntity policyEntity);

    // Map PolicyDto to PolicyEntity
    PolicyEntity toEntity(PolicyDto policyDto);

    // Map PolicyEntity to PremiumScheduleDto
    PremiumScheduleDto toPremiumScheduleDto(PolicyEntity policyEntity);
}