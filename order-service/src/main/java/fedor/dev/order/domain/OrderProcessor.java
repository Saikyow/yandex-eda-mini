package fedor.dev.order.domain;

import fedor.dev.api.http.order.OrderStatus;
import fedor.dev.api.http.order.CreateOrderRequestDto;
import fedor.dev.api.http.payment.CreatePaymentRequestDto;
import fedor.dev.api.http.payment.CreatePaymentResponseDto;
import fedor.dev.api.http.payment.PaymentStatus;
import fedor.dev.api.kafka.DeliveryAssignedEvent;
import fedor.dev.api.kafka.OrderPaidEvent;
import fedor.dev.order.api.OrderPaymentRequest;
import fedor.dev.order.domain.db.OrderEntity;
import fedor.dev.order.domain.db.OrderEntityMapper;
import fedor.dev.order.domain.db.OrderItemEntity;
import fedor.dev.order.domain.db.OrderJpaRepository;
import fedor.dev.order.external.PaymentHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderProcessor {

    private final OrderJpaRepository orderItemJpaRepository;
    private final PaymentHttpClient paymentHttpClient;
    private final OrderEntityMapper orderEntityMapper;
    private final KafkaTemplate<Long, OrderPaidEvent> kafkaTemplate;

    @Value("${order-paid-topic}")
    private String orderPaidTopic;

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
                ? OrderStatus.PAID
                : OrderStatus.PAYMENT_FAILED;



        entity.setOrderStatus(status);
        sendOrderPaidEvent(entity, response);
        return orderItemJpaRepository.save(entity);

    }

    private void sendOrderPaidEvent(OrderEntity entity, CreatePaymentResponseDto paymentResponseDto) {
        kafkaTemplate.send(
                orderPaidTopic,
                entity.getId(),
                OrderPaidEvent.builder()
                        .orderId(entity.getId())
                        .amount(entity.getTotalAmount())
                        .paymentMethod(paymentResponseDto.paymentMethod())
                        .paymentId(paymentResponseDto.paymentId())
                        .build()
        ).thenAccept(result -> {
            log.info("Order Paid event sent with id={}", entity.getId());
        })
        ;

    }

    public void processDeliveryAssigned(DeliveryAssignedEvent event) {
        var order = getOrderOrThrow(event.orderId());

        if (!order.getOrderStatus().equals(OrderStatus.PAID)){
            processIncorrectDeliveryState(order);
            return;
        }
        order.setOrderStatus(OrderStatus.DELIVERY_ASSIGNED);
        order.setCourierName(event.courierName());
        order.setEtaMinutes(event.etaMinutes());
        orderItemJpaRepository.save(order);
        log.info("Order delivery assigned processed: orderId={}", event.orderId());
    }

    private void processIncorrectDeliveryState(OrderEntity order) {
        if (order.getOrderStatus().equals(OrderStatus.DELIVERY_ASSIGNED)) {
            log.info("Order delivery assigned processed: orderId={}", order.getId());
        } else {
            log.error("Trying to assign delivery but order have incorrect state: orderId={}", order.getId());

        }
    }
}
