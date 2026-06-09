package com.ronnefeldt.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import com.ronnefeldt.entity.CartItemEntity;
import com.ronnefeldt.entity.MemberEntity;
import com.ronnefeldt.entity.OrderEntity;
import com.ronnefeldt.entity.OrderItemEntity;
import com.ronnefeldt.model.LoginUser;
import com.ronnefeldt.model.OrderItem;
import com.ronnefeldt.model.OrderSummary;
import com.ronnefeldt.repository.jpa.CartItemJpaRepository;
import com.ronnefeldt.repository.jpa.MemberJpaRepository;
import com.ronnefeldt.repository.jpa.OrderJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final OrderJpaRepository orderJpaRepository;
    private final MemberJpaRepository memberJpaRepository;
    private final CartItemJpaRepository cartItemJpaRepository;

    public OrderService(
        OrderJpaRepository orderJpaRepository,
        MemberJpaRepository memberJpaRepository,
        CartItemJpaRepository cartItemJpaRepository
    ) {
        this.orderJpaRepository = orderJpaRepository;
        this.memberJpaRepository = memberJpaRepository;
        this.cartItemJpaRepository = cartItemJpaRepository;
    }

    @Transactional
    public long createOrderFromCart(LoginUser loginUser, List<Long> cartItemIds, String deliveryType) {
        MemberEntity member = findMember(loginUser);
        List<CartItemEntity> cartItems = findOrderCartItems(loginUser, cartItemIds, deliveryType);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("주문할 장바구니 상품이 없습니다.");
        }

        int totalAmount = cartItems.stream()
            .mapToInt(item -> item.getQuantity() * item.getUnitPrice())
            .sum();
        OrderEntity order = new OrderEntity(member, "R" + ORDER_NO_FORMAT.format(LocalDateTime.now()), totalAmount);
        cartItems.stream()
            .map(OrderItemEntity::new)
            .forEach(order::addItem);

        return orderJpaRepository.save(order).getId();
    }

    @Transactional(readOnly = true)
    public Optional<OrderSummary> findOrder(LoginUser loginUser, long orderId) {
        return orderJpaRepository.findByIdAndMemberProviderAndMemberProviderUserId(
                orderId,
                normalizeProvider(loginUser.provider()),
                loginUser.providerId()
            )
            .map(this::toSummary);
    }

    private MemberEntity findMember(LoginUser loginUser) {
        return memberJpaRepository.findByProviderAndProviderUserId(
                normalizeProvider(loginUser.provider()),
                loginUser.providerId()
            )
            .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));
    }

    private List<CartItemEntity> findOrderCartItems(LoginUser loginUser, List<Long> cartItemIds, String deliveryType) {
        List<CartItemEntity> allItems = cartItemJpaRepository
            .findByCartMemberProviderAndCartMemberProviderUserIdAndDeliveryTypeOrderByIdDesc(
                normalizeProvider(loginUser.provider()),
                loginUser.providerId(),
                normalizeDeliveryType(deliveryType)
            );
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return allItems;
        }
        Set<Long> selectedIds = new HashSet<>(cartItemIds);
        return allItems.stream()
            .filter(item -> selectedIds.contains(item.getId()))
            .toList();
    }

    private OrderSummary toSummary(OrderEntity order) {
        List<OrderItem> items = order.getItems().stream()
            .map(item -> new OrderItem(
                item.getId(),
                item.getProduct().getId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
            ))
            .toList();
        return new OrderSummary(
            order.getId(),
            order.getOrderNo(),
            order.getOrderStatus(),
            order.getTotalAmount(),
            items
        );
    }

    private String normalizeDeliveryType(String deliveryType) {
        return "B".equalsIgnoreCase(deliveryType) || "OVERSEAS".equalsIgnoreCase(deliveryType) ? "OVERSEAS" : "DOMESTIC";
    }

    private String normalizeProvider(String provider) {
        return provider == null || provider.isBlank() ? "LOCAL" : provider.toUpperCase(Locale.ROOT);
    }
}
