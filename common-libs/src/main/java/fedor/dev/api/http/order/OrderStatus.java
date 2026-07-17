package fedor.dev.api.http.order;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    DELIVERY_ASSIGNED,
    PAYMENT_FAILED,
    DELIVERED;
}
