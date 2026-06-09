package com.ronnefeldt.controller;

import com.ronnefeldt.service.AccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {

    private final AccountService accountService;

    public MainController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/member/login.html")
    public String login() {
        return "account/login";
    }

    @PostMapping("/member/login.html")
    public String login(
        @RequestParam String memberId,
        @RequestParam String memberPassword,
        HttpSession session,
        Model model
    ) {
        try {
            session.setAttribute("loginUser", accountService.login(memberId, memberPassword));
            return "redirect:/";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("memberId", memberId);
            return "account/login";
        }
    }

    @GetMapping("/member/join.html")
    public String join() {
        return "account/join";
    }

    @PostMapping("/member/join.html")
    public String join(
        @RequestParam String memberId,
        @RequestParam String password,
        @RequestParam String passwordConfirm,
        @RequestParam String name,
        @RequestParam String email,
        @RequestParam(name = "agreeTerms", defaultValue = "false") boolean agreeTerms,
        HttpSession session,
        Model model
    ) {
        try {
            session.setAttribute("loginUser", accountService.join(memberId, password, passwordConfirm, name, email, agreeTerms));
            return "redirect:/";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("memberId", memberId);
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            return "account/join";
        }
    }

    @GetMapping("/myshop/index.html")
    public String myshop(HttpSession session, Model model) {
        if (session.getAttribute("loginUser") == null) {
            return "redirect:/member/login.html";
        }
        model.addAttribute("mileage", 0);
        model.addAttribute("totalMileage", 0);
        model.addAttribute("usedMileage", 0);
        model.addAttribute("couponCount", 0);
        model.addAttribute("totalOrderAmount", 0);
        model.addAttribute("totalOrderCount", 0);
        return "myshop/index";
    }

    @GetMapping("/member/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

}
