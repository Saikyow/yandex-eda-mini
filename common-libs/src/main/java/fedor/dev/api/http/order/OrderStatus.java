package fedor.dev.api.http.order;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    PENDING_DELIVERY,
    PAYMENT_FAILED,
    DELIVERED;
}
