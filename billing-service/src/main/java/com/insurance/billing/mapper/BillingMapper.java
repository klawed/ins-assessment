package com.insurance.billing.mapper;

import com.insurance.billing.entity.Billing;
import com.insurance.shared.dto.BillingDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BillingMapper {
    
    @Mapping(target = "status", source = "status")
    @Mapping(target = "paymentStatus", source = "paymentStatus")
    BillingDto toDto(Billing billing);

    @Mapping(target = "status", source = "status")
    @Mapping(target = "paymentStatus", source = "paymentStatus")
    Billing toEntity(BillingDto dto);

    List<BillingDto> toDtoList(List<Billing> billings);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(BillingDto dto, @MappingTarget Billing billing);
}