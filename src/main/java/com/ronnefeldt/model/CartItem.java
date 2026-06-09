package com.ronnefeldt.model;

import java.text.NumberFormat;
import java.util.Locale;

public record CartItem(
    long id,
    long productId,
    String productName,
    String productSummary,
    String imageUrl,
    int quantity,
    int unitPrice,
    String deliveryType
) {

    public int totalPrice() {
        return unitPrice * quantity;
    }

    public String formattedUnitPrice() {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(unitPrice) + "원";
    }

    public String formattedTotalPrice() {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(totalPrice()) + "원";
    }
}
