package com.ronnefeldt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;

import com.ronnefeldt.model.LoginUser;
import com.ronnefeldt.repository.jpa.PaymentJpaRepository;
import com.ronnefeldt.service.CartService;
import com.ronnefeldt.service.OrderService;
import com.ronnefeldt.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class CommerceJpaFlowTests {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

    @Test
    void cartOrderPaymentFlowUsesJpaAndClearsPaidCartItems() {
        LoginUser user = new LoginUser("naver", "jpa-flow-user", "jpa-flow@example.com", "테스트회원", "테스트");

        cartService.addItem(user, 18L, 2, "A");

        var cartItems = cartService.findItems(user, "DOMESTIC");
        assertThat(cartItems).hasSize(1);
        assertThat(cartItems.get(0).productName()).isEqualTo("Rich Aroma LeafCup");
        assertThat(cartItems.get(0).quantity()).isEqualTo(2);
        assertThat(cartItems.get(0).totalPrice()).isEqualTo(50_000);
        assertThat(cartService.findTotalQuantity(user)).isEqualTo(2);

        long orderId = orderService.createOrderFromCart(user, List.of(), "A");

        var createdOrder = orderService.findOrder(user, orderId).orElseThrow();
        assertThat(createdOrder.orderStatus()).isEqualTo("CREATED");
        assertThat(createdOrder.totalAmount()).isEqualTo(50_000);
        assertThat(createdOrder.items()).hasSize(1);

        paymentService.markPaid(orderId, "test-payment-key", "EASY_PAY", 50_000, "{\"status\":\"PAID\"}", OffsetDateTime.now());

        var paidOrder = orderService.findOrder(user, orderId).orElseThrow();
        assertThat(paidOrder.orderStatus()).isEqualTo("PAID");
        assertThat(cartService.findItems(user, "DOMESTIC")).isEmpty();
        assertThat(cartService.findTotalQuantity(user)).isZero();
        assertThat(paymentJpaRepository.count()).isEqualTo(1);
    }
}
