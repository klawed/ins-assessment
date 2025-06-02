package com.insurance.payment.mapper;

import org.mapstruct.Mapper;
import com.insurance.shared.dto.PaymentDto;
import com.insurance.payment.entity.PaymentEntity;


@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentDto toDto(PaymentEntity entity);
    PaymentEntity toEntity(PaymentDto dto);
}
