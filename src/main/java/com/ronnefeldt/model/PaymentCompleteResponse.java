package com.ronnefeldt.model;

public record PaymentCompleteResponse(
    boolean success,
    String message,
    String redirectUrl
) {
    public static PaymentCompleteResponse success(long orderId) {
        return new PaymentCompleteResponse(true, "결제가 확인되었습니다.", "/order/complete.html?orderId=" + orderId);
    }

    public static PaymentCompleteResponse fail(String message) {
        return new PaymentCompleteResponse(false, message, null);
    }
}
