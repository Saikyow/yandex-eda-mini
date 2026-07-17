package fedor.dev.order.api;

import fedor.dev.api.http.payment.PaymentMethod;

public record OrderPaymentRequest (
//        Long orderId,
        PaymentMethod paymentMethod
){

}
