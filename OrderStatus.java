public enum OrderStatus {
    PENDING,
    RESERVED,
    OUT_OF_STOCK,
    PAID,
    PAYMENT_FAILED,
    CANCELLED,
    PACKED,
    DISPATCHED,
    DELIVERED;

    public boolean isFinal() {
        return this == OUT_OF_STOCK || this == PAYMENT_FAILED
                || this == CANCELLED || this == DELIVERED;
    }
}
