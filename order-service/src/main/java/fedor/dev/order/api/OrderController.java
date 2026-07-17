package fedor.dev.order.api;


import fedor.dev.api.http.order.CreateOrderRequestDto;
import fedor.dev.api.http.order.OrderDto;
import fedor.dev.order.domain.db.OrderEntityMapper;
import fedor.dev.order.domain.OrderProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderProcessor orderProcessor;

    private final OrderEntityMapper orderEntityMapper;

    @PostMapping("/{id}/pay")
    public OrderDto payOrder(
            @PathVariable Long id,
            @RequestBody OrderPaymentRequest request
    ) {
        log.info("Paying orde with id={}, request={}", id, request);
        var entity = orderProcessor.processPayment(id, request);
        return orderEntityMapper.toOrderDto(entity);
    }

    @GetMapping("/{id}")
    public OrderDto getOne(@PathVariable Long id) {
        log.info("Retrieving order with id {}", id);
        var found = orderProcessor.getOrderOrThrow(id);
        return orderEntityMapper.toOrderDto(found);
    }


}
