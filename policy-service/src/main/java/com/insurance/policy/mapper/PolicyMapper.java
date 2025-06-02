package com.insurance.policy.mapper;

import com.insurance.policy.entity.PolicyEntity;
import com.insurance.shared.dto.PolicyDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PolicyMapper {
    PolicyDto toDto(PolicyEntity entity);
    PolicyEntity toEntity(PolicyDto dto);
}