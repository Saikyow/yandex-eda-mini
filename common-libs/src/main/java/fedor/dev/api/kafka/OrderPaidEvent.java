package fedor.dev.api.kafka;

import fedor.dev.api.http.payment.PaymentMethod;
import lombok.Builder;

import java.math.BigDecimal;
@Builder
public record OrderPaidEvent(
        Long orderId,
        Long paymentId,
        BigDecimal amount,
        PaymentMethod  paymentMethod
) {
}
