package fedor.dev.api.http.order;


import java.math.BigDecimal;

/**
 */
public record OrderItemEntityDto(
        Long id,
        Long itemId,
        Integer quantity,
        BigDecimal priceAtPurchase) {
}