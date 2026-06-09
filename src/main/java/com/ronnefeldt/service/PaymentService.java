package com.ronnefeldt.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import com.ronnefeldt.entity.CartItemEntity;
import com.ronnefeldt.entity.OrderEntity;
import com.ronnefeldt.entity.PaymentEntity;
import com.ronnefeldt.repository.jpa.CartItemJpaRepository;
import com.ronnefeldt.repository.jpa.OrderJpaRepository;
import com.ronnefeldt.repository.jpa.PaymentJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentJpaRepository paymentJpaRepository;
    private final OrderJpaRepository orderJpaRepository;
    private final CartItemJpaRepository cartItemJpaRepository;

    public PaymentService(
        PaymentJpaRepository paymentJpaRepository,
        OrderJpaRepository orderJpaRepository,
        CartItemJpaRepository cartItemJpaRepository
    ) {
        this.paymentJpaRepository = paymentJpaRepository;
        this.orderJpaRepository = orderJpaRepository;
        this.cartItemJpaRepository = cartItemJpaRepository;
    }

    @Transactional
    public void markPaid(long orderId, String paymentKey, String method, int amount, String rawResponse, OffsetDateTime approvedAt) {
        OrderEntity order = findOrder(orderId);
        PaymentEntity payment = paymentJpaRepository.findByOrder(order)
            .orElseGet(() -> new PaymentEntity(order));
        payment.markDone(paymentKey, method, amount, rawResponse, approvedAt);
        paymentJpaRepository.save(payment);

        order.markPaid();
        List<CartItemEntity> paidCartItems = order.getItems().stream()
            .map(item -> item.getCartItem())
            .filter(Objects::nonNull)
            .toList();
        order.getItems().forEach(item -> item.detachCartItem());
        orderJpaRepository.flush();
        cartItemJpaRepository.deleteAll(paidCartItems);
    }

    @Transactional
    public void markFailed(long orderId, String paymentKey, String method, int amount, String rawResponse) {
        OrderEntity order = findOrder(orderId);
        PaymentEntity payment = paymentJpaRepository.findByOrder(order)
            .orElseGet(() -> new PaymentEntity(order));
        payment.markFailed(paymentKey, method, amount, rawResponse);
        paymentJpaRepository.save(payment);
    }

    private OrderEntity findOrder(long orderId) {
        return orderJpaRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
    }
}
