package fedor.dev.payment.domain;

import fedor.dev.payment.domain.db.PaymentEntityMapper;
import fedor.dev.payment.domain.db.PaymentEntityRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class PaymentService {

    private final PaymentEntityMapper mapper;
    private final PaymentEntityRepository repository;



    public CreatePaymentResponseDto makePayment(CreatePaymentRequestDto request){

        var found = repository.findByOrderId(request.orderId());
        if (found.isPresent()) {
            log.info("Payment already exists for order id {}", request.orderId());
            return mapper.toResponseDto(found.get());
        }

        var entity = mapper.toEntity(request);

        var status = request.paymentMethod().equals(PaymentMethod.QR)
                ? PaymentStatus.PAYMENT_FAILED
                : PaymentStatus.PAYMENT_SUCCEEDED;

        entity.setPaymentStatus(status);

        var savedEntity = repository.save(entity);
        return mapper.toResponseDto(savedEntity);
    }
}
