package fedor.dev.order.domain;

import fedor.dev.api.http.order.OrderStatus;
import fedor.dev.api.http.order.CreateOrderRequestDto;
import fedor.dev.api.http.payment.CreatePaymentRequestDto;
import fedor.dev.api.http.payment.PaymentStatus;
import fedor.dev.order.api.OrderPaymentRequest;
import fedor.dev.order.domain.db.OrderEntity;
import fedor.dev.order.domain.db.OrderEntityMapper;
import fedor.dev.order.domain.db.OrderItemEntity;
import fedor.dev.order.domain.db.OrderJpaRepository;
import fedor.dev.order.external.PaymentHttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Service
public class OrderProcessor {

    private final OrderJpaRepository orderItemJpaRepository;
    private final PaymentHttpClient paymentHttpClient;
    private final OrderEntityMapper orderEntityMapper;

    public OrderEntity create(CreateOrderRequestDto request) {
        var entity = orderEntityMapper.toEntity(request);
        calculatePricingsForOrder(entity);
        entity.setOrderStatus(OrderStatus.PENDING_PAYMENT);
        return orderItemJpaRepository.save(entity);
    }



    public OrderEntity getOrderOrThrow(Long id) {
        var orderEntityOptional = orderItemJpaRepository.findById(id);
        return orderEntityOptional
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Entity with id `%s` not found".formatted(id)));
    }

    private void calculatePricingsForOrder(OrderEntity entity) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItemEntity item : entity.getItems()) {
            var randomPrice = ThreadLocalRandom.current().nextDouble(100, 5000);
            item.setPriceAtPurchase(BigDecimal.valueOf(randomPrice));

            totalPrice = item.getPriceAtPurchase()
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .add(totalPrice);
        }
        entity.setTotalAmount(totalPrice);
    }

    public OrderEntity processPayment(
            Long id,
            OrderPaymentRequest request
    ) {
        var entity = getOrderOrThrow(id);
        if (!entity.getOrderStatus().equals(OrderStatus.PENDING_PAYMENT)) {
            throw new RuntimeException("Order status must be in status PENDING_PAYMENT");
        }
        var response = paymentHttpClient.createPayment(CreatePaymentRequestDto.builder()
                        .orderId(id)
                        .paymentMethod(request.paymentMethod())
                        .amount(entity.getTotalAmount())
                        .build());

        var status = response.paymentStatus().equals(PaymentStatus.PAYMENT_SUCCEEDED)
                ? OrderStatus.PAYMENT_FAILED
                : OrderStatus.PAID;

        entity.setOrderStatus(status);
        return orderItemJpaRepository.save(entity);

    }
}
