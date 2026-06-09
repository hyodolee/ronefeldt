package com.ronnefeldt.controller;

import java.util.List;

import com.ronnefeldt.model.CartItem;
import com.ronnefeldt.model.LoginUser;
import com.ronnefeldt.model.OrderItem;
import com.ronnefeldt.service.CartService;
import com.ronnefeldt.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;
    private final String portOneStoreId;
    private final String portOneChannelKey;
    private final String portOnePayMethod;
    private final String portOneEasyPayProvider;

    public CartController(
        CartService cartService,
        OrderService orderService,
        @Value("${portone.store-id:}") String portOneStoreId,
        @Value("${portone.channel-key:}") String portOneChannelKey,
        @Value("${portone.pay-method:EASY_PAY}") String portOnePayMethod,
        @Value("${portone.easy-pay-provider:}") String portOneEasyPayProvider
    ) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.portOneStoreId = portOneStoreId;
        this.portOneChannelKey = portOneChannelKey;
        this.portOnePayMethod = portOnePayMethod;
        this.portOneEasyPayProvider = portOneEasyPayProvider;
    }

    @GetMapping("/order/basket.html")
    public String basket(@RequestParam(defaultValue = "A") String delvtype, HttpSession session, Model model) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        String deliveryType = "B".equalsIgnoreCase(delvtype) ? "OVERSEAS" : "DOMESTIC";
        List<CartItem> cartItems = cartService.findItems(loginUser, deliveryType);
        int totalAmount = cartItems.stream().mapToInt(CartItem::totalPrice).sum();
        int totalQuantity = cartItems.stream().mapToInt(CartItem::quantity).sum();
        int cartCount = cartService.findTotalQuantity(loginUser);

        session.setAttribute("cartCount", cartCount);
        model.addAttribute("delvtype", delvtype);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("cartCount", cartCount);
        model.addAttribute("domesticCount", "B".equalsIgnoreCase(delvtype) ? 0 : totalQuantity);
        model.addAttribute("overseasCount", "B".equalsIgnoreCase(delvtype) ? totalQuantity : 0);
        return "order/basket";
    }

    @PostMapping("/cart/items")
    public String addCartItem(
        @RequestParam long productId,
        @RequestParam(defaultValue = "1") int quantity,
        @RequestParam(defaultValue = "A") String delvtype,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("cartMessage", "로그인 후 장바구니를 이용해주세요.");
            return "redirect:/member/login.html";
        }

        cartService.addItem(loginUser, productId, quantity, delvtype);
        refreshCartCount(session, loginUser);
        return redirectBasket(delvtype);
    }

    @PostMapping("/cart/items/update")
    public String updateCartItem(
        @RequestParam long cartItemId,
        @RequestParam(defaultValue = "1") int quantity,
        @RequestParam(defaultValue = "A") String delvtype,
        HttpSession session
    ) {
        LoginUser loginUser = requireLogin(session);
        cartService.updateQuantity(loginUser, cartItemId, quantity);
        refreshCartCount(session, loginUser);
        return redirectBasket(delvtype);
    }

    @PostMapping("/cart/items/delete")
    public String deleteCartItem(
        @RequestParam long cartItemId,
        @RequestParam(defaultValue = "A") String delvtype,
        HttpSession session
    ) {
        LoginUser loginUser = requireLogin(session);
        cartService.deleteItem(loginUser, cartItemId);
        refreshCartCount(session, loginUser);
        return redirectBasket(delvtype);
    }

    @PostMapping("/order/prepare")
    public String prepareOrder(
        @RequestParam(defaultValue = "all") String mode,
        @RequestParam(required = false) List<Long> cartItemIds,
        @RequestParam(defaultValue = "A") String delvtype,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("cartMessage", "로그인 후 주문을 진행해주세요.");
            return "redirect:/member/login.html";
        }

        List<Long> selectedIds = "selected".equalsIgnoreCase(mode) ? cartItemIds : List.of();
        if ("selected".equalsIgnoreCase(mode) && (selectedIds == null || selectedIds.isEmpty())) {
            redirectAttributes.addFlashAttribute("cartMessage", "주문할 상품을 선택해주세요.");
            return redirectBasket(delvtype);
        }

        long orderId = orderService.createOrderFromCart(loginUser, selectedIds, delvtype);
        return "redirect:/order/orderform.html?orderId=" + orderId;
    }

    @GetMapping("/order/orderform.html")
    public String orderForm(@RequestParam long orderId, HttpSession session, Model model) {
        LoginUser loginUser = requireLogin(session);
        var order = orderService.findOrder(loginUser, orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        model.addAttribute("order", order);
        model.addAttribute("paymentId", order.orderNo());
        model.addAttribute("orderName", buildOrderName(order.items()));
        model.addAttribute("portOneStoreId", portOneStoreId);
        model.addAttribute("portOneChannelKey", portOneChannelKey);
        model.addAttribute("portOnePayMethod", portOnePayMethod);
        model.addAttribute("portOneEasyPayProvider", portOneEasyPayProvider);
        model.addAttribute("portOneConfigured", !portOneStoreId.isBlank() && !portOneChannelKey.isBlank());
        return "order/orderform";
    }

    @GetMapping("/order/complete.html")
    public String complete(@RequestParam long orderId, HttpSession session, Model model) {
        LoginUser loginUser = requireLogin(session);
        var order = orderService.findOrder(loginUser, orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        model.addAttribute("order", order);
        return "order/complete";
    }

    private void refreshCartCount(HttpSession session, LoginUser loginUser) {
        session.setAttribute("cartCount", cartService.findTotalQuantity(loginUser));
    }

    private LoginUser requireLogin(HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return loginUser;
    }

    private String redirectBasket(String delvtype) {
        return "redirect:/order/basket.html" + ("B".equalsIgnoreCase(delvtype) ? "?delvtype=B" : "");
    }

    private String buildOrderName(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return "Ronnefeldt 주문";
        }
        String firstName = items.get(0).productName();
        return items.size() == 1 ? firstName : firstName + " 외 " + (items.size() - 1) + "건";
    }
}
