package com.ronnefeldt.controller;

import com.ronnefeldt.model.LoginUser;
import com.ronnefeldt.model.PaymentCompleteRequest;
import com.ronnefeldt.model.PaymentCompleteResponse;
import com.ronnefeldt.service.OrderService;
import com.ronnefeldt.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@RestController
public class PaymentController {

    private static final Pattern STATUS_PATTERN = Pattern.compile("\"status\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern AMOUNT_TOTAL_PATTERN = Pattern.compile("\"amount\"\\s*:\\s*\\{[^}]*\"total\"\\s*:\\s*(\\d+)", Pattern.DOTALL);
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("\"amount\"\\s*:\\s*(\\d+)");
    private static final Pattern PAID_AT_PATTERN = Pattern.compile("\"(?:paidAt|approvedAt)\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern METHOD_TYPE_PATTERN = Pattern.compile("\"method\"\\s*:\\s*\\{[^}]*\"type\"\\s*:\\s*\"([^\"]+)\"", Pattern.DOTALL);
    private static final Pattern METHOD_PATTERN = Pattern.compile("\"method\"\\s*:\\s*\"([^\"]+)\"");

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final RestClient portOneClient;
    private final String storeId;
    private final String apiSecret;

    public PaymentController(
        OrderService orderService,
        PaymentService paymentService,
        @Value("${portone.store-id:}") String storeId,
        @Value("${portone.api-secret:}") String apiSecret
    ) {
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.storeId = storeId;
        this.apiSecret = apiSecret;
        this.portOneClient = RestClient.builder().baseUrl("https://api.portone.io").build();
    }

    @PostMapping("/payments/complete")
    public ResponseEntity<PaymentCompleteResponse> complete(
        @RequestBody PaymentCompleteRequest request,
        HttpSession session
    ) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(PaymentCompleteResponse.fail("로그인이 필요합니다."));
        }
        if (isBlank(storeId) || isBlank(apiSecret)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(PaymentCompleteResponse.fail("PortOne 설정이 없습니다."));
        }
        if (request == null || request.orderId() <= 0 || isBlank(request.paymentId())) {
            return ResponseEntity.badRequest()
                .body(PaymentCompleteResponse.fail("결제 요청 정보가 올바르지 않습니다."));
        }

        var order = orderService.findOrder(loginUser, request.orderId()).orElse(null);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(PaymentCompleteResponse.fail("주문을 찾을 수 없습니다."));
        }
        if (!order.orderNo().equals(request.paymentId())) {
            return ResponseEntity.badRequest()
                .body(PaymentCompleteResponse.fail("주문번호와 결제번호가 일치하지 않습니다."));
        }

        String payment;
        try {
            payment = fetchPayment(request.paymentId());
        } catch (RestClientResponseException exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(PaymentCompleteResponse.fail("PortOne 결제 조회에 실패했습니다. HTTP " + exception.getStatusCode().value()));
        } catch (RestClientException exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(PaymentCompleteResponse.fail("PortOne 결제 조회에 실패했습니다."));
        }

        String status = textOf(payment, STATUS_PATTERN);
        int paidAmount = amountOf(payment);
        String method = paymentMethodOf(payment);
        OffsetDateTime paidAt = dateTimeAt(payment);

        if (!"PAID".equalsIgnoreCase(status)) {
            paymentService.markFailed(order.id(), request.paymentId(), method, paidAmount, payment);
            return ResponseEntity.badRequest()
                .body(PaymentCompleteResponse.fail("아직 결제 완료 상태가 아닙니다. status=" + status));
        }
        if (paidAmount != order.totalAmount()) {
            paymentService.markFailed(order.id(), request.paymentId(), method, paidAmount, payment);
            return ResponseEntity.badRequest()
                .body(PaymentCompleteResponse.fail("주문 금액과 결제 금액이 다릅니다. order=" + order.totalAmount() + ", paid=" + paidAmount));
        }

        try {
            paymentService.markPaid(order.id(), request.paymentId(), method, paidAmount, payment, paidAt);
            session.setAttribute("cartCount", 0);
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PaymentCompleteResponse.fail("결제는 확인됐지만 DB 저장에 실패했습니다."));
        }
        return ResponseEntity.ok(PaymentCompleteResponse.success(order.id()));
    }

    private String fetchPayment(String paymentId) {
        String payment = portOneClient.get()
            .uri("/payments/{paymentId}", paymentId)
            .header(HttpHeaders.AUTHORIZATION, "PortOne " + apiSecret)
            .retrieve()
            .body(String.class);
        if (payment == null || payment.isBlank()) {
            throw new RestClientException("Empty PortOne response");
        }
        return payment;
    }

    private int amountOf(String payment) {
        String totalAmount = textOf(payment, AMOUNT_TOTAL_PATTERN);
        if (!totalAmount.isBlank()) {
            return Integer.parseInt(totalAmount);
        }
        String amount = textOf(payment, AMOUNT_PATTERN);
        return amount.isBlank() ? 0 : Integer.parseInt(amount);
    }

    private String paymentMethodOf(String payment) {
        String type = textOf(payment, METHOD_TYPE_PATTERN);
        if (!type.isBlank()) {
            return type;
        }
        String method = textOf(payment, METHOD_PATTERN);
        return method.isBlank() ? "UNKNOWN" : method;
    }

    private String textOf(String source, Pattern pattern) {
        if (source == null) {
            return "";
        }
        Matcher matcher = pattern.matcher(source);
        return matcher.find() ? matcher.group(1) : "";
    }

    private OffsetDateTime dateTimeAt(String payment) {
        String value = textOf(payment, PAID_AT_PATTERN);
        if (value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
