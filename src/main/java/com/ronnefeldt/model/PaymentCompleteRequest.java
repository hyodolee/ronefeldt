package com.ronnefeldt.model;

public record PaymentCompleteRequest(
    long orderId,
    String paymentId
) {
}
