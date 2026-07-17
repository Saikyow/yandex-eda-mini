package fedor.dev.payment.api;

import fedor.dev.payment.domain.db.PaymentEntity;

import java.math.BigDecimal;
import javax.annotation.processing.Generated;

import fedor.dev.payment.domain.db.PaymentEntityMapper;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-12T22:38:26+0700",
    comments = "version: 1.6.0, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.jar, environment: Java 21.0.11 (Homebrew)"
)
@Component
public class PaymentEntityMapperImpl implements PaymentEntityMapper {

    @Override
    public PaymentEntity toEntity(CreatePaymentRequestDto request) {
        if ( request == null ) {
            return null;
        }

        PaymentEntity paymentEntity = new PaymentEntity();

        paymentEntity.setOrderId( request.orderId() );
        paymentEntity.setAmount( request.amount() );
        paymentEntity.setPaymentMethod( request.paymentMethod() );

        return paymentEntity;
    }

    @Override
    public CreatePaymentResponseDto toResponseDto(PaymentEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Long paymentId = null;
        PaymentStatus paymentStatus = null;
        Long orderId = null;
        PaymentMethod paymentMethod = null;
        BigDecimal amount = null;

        paymentId = entity.getId();
        paymentStatus = entity.getPaymentStatus();
        orderId = entity.getOrderId();
        paymentMethod = entity.getPaymentMethod();
        amount = entity.getAmount();

        CreatePaymentResponseDto createPaymentResponseDto = new CreatePaymentResponseDto( paymentId, paymentStatus, orderId, paymentMethod, amount );

        return createPaymentResponseDto;
    }
}
