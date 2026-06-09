package com.ronnefeldt.entity;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(nullable = false, length = 50)
    private String provider = "PORTONE";

    @Column(name = "payment_key", length = 255)
    private String paymentKey;

    @Column(nullable = false, length = 30)
    private String status = "READY";

    @Column(length = 50)
    private String method;

    @Column(nullable = false)
    private int amount;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;

    protected PaymentEntity() {
    }

    public PaymentEntity(OrderEntity order) {
        this.order = order;
    }

    public void markDone(String paymentKey, String method, int amount, String rawResponse, OffsetDateTime approvedAt) {
        this.provider = "PORTONE";
        this.paymentKey = paymentKey;
        this.status = "DONE";
        this.method = method;
        this.amount = amount;
        this.rawResponse = normalizeRawResponse(rawResponse);
        this.approvedAt = approvedAt == null ? null : approvedAt.toLocalDateTime();
    }

    public void markFailed(String paymentKey, String method, int amount, String rawResponse) {
        this.provider = "PORTONE";
        this.paymentKey = paymentKey;
        this.status = "FAILED";
        this.method = method;
        this.amount = amount;
        this.rawResponse = normalizeRawResponse(rawResponse);
    }

    private String normalizeRawResponse(String rawResponse) {
        return rawResponse == null || rawResponse.isBlank() ? "{}" : rawResponse;
    }
}
