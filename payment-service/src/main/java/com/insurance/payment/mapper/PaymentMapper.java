package com.insurance.payment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentDto toDto(PaymentEntity entity);
    PaymentEntity toEntity(PaymentDto dto);
}
