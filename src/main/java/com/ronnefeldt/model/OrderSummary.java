package com.ronnefeldt.model;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public record OrderSummary(
    long id,
    String orderNo,
    String orderStatus,
    int totalAmount,
    List<OrderItem> items
) {

    public String formattedTotalAmount() {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(totalAmount) + "원";
    }
}
