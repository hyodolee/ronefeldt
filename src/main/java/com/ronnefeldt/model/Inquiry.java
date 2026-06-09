package com.ronnefeldt.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Inquiry(
    long id,
    String title,
    String content,
    String writer,
    Long productId,
    String productName,
    String status,
    LocalDateTime createdAt,
    boolean answered
) {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String displayStatus() {
        return answered ? "답변완료" : "답변대기";
    }

    public String displayDate() {
        return createdAt == null ? "" : createdAt.format(DATE_FORMAT);
    }

    public String displayProductName() {
        return productName == null || productName.isBlank() ? "-" : productName;
    }
}
