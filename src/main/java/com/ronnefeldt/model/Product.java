package com.ronnefeldt.model;

import java.text.NumberFormat;
import java.util.Locale;

public record Product(
    long id,
    long categoryId,
    String categorySlug,
    String categoryName,
    String name,
    String summary,
    String description,
    int price,
    String mainImageUrl,
    boolean soldOut,
    int displayOrder
) {

    public String slug() {
        return name.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9가-힣]+", "-")
            .replaceAll("(^-|-$)", "");
    }

    public String formattedPrice() {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(price) + "원";
    }

    public String detailUrl() {
        return "/product/%s/%d/category/%d/display/1/".formatted(slug(), id, categoryId);
    }

    public String displaySummary() {
        if (summary == null || summary.isBlank()) {
            return "단독구매상품";
        }
        return summary;
    }

    public String displayDescription() {
        if (description == null || description.isBlank()) {
            return displaySummary();
        }
        return description;
    }

    public String displayImageUrl() {
        return mainImageUrl;
    }
}
