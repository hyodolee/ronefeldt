package com.ronnefeldt.controller;

import com.ronnefeldt.model.LoginUser;
import com.ronnefeldt.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    private final CartService cartService;

    public GlobalModelAdvice(CartService cartService) {
        this.cartService = cartService;
    }

    @ModelAttribute("cartCount")
    public int cartCount(HttpSession session) {
        Object cachedCartCount = session.getAttribute("cartCount");
        if (cachedCartCount instanceof Integer count) {
            return count;
        }

        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        int cartCount = cartService.findTotalQuantity(loginUser);
        if (loginUser != null) {
            session.setAttribute("cartCount", cartCount);
        }
        return cartCount;
    }
}
