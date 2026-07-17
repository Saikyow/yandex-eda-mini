package fedor.dev.payment.domain.db;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentEntityMapper {

    PaymentEntity toEntity(CreatePaymentRequestDto request);

    @Mapping(source = "id", target = "paymentId")
    CreatePaymentResponseDto toResponseDto(PaymentEntity entity);

}
