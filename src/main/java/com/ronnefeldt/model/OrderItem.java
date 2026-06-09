package com.ronnefeldt.model;

import java.text.NumberFormat;
import java.util.Locale;

public record OrderItem(
    long id,
    long productId,
    String productName,
    int quantity,
    int unitPrice,
    int totalPrice
) {

    public String formattedUnitPrice() {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(unitPrice) + "원";
    }

    public String formattedTotalPrice() {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(totalPrice) + "원";
    }
}
