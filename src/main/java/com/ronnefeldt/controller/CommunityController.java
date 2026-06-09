package com.ronnefeldt.controller;

import com.ronnefeldt.model.LoginUser;
import com.ronnefeldt.service.InquiryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CommunityController {

    private final InquiryService inquiryService;

    public CommunityController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @GetMapping({"/board/product/6/", "/community/inquiries"})
    public String inquiryList(Model model) {
        model.addAttribute("active", "inquiry");
        model.addAttribute("inquiries", inquiryService.findAll());
        return "community/inquiries";
    }

    @GetMapping("/board/product/write.html")
    public String inquiryWrite(
        @RequestParam(name = "board_no", defaultValue = "6") int boardNo,
        @RequestParam(name = "product_no", required = false) Long productNo,
        HttpSession session,
        Model model
    ) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/member/login.html";
        }
        if (boardNo == 4) {
            model.addAttribute("active", "inquiry");
            return "community/review-write";
        }
        if (boardNo != 6) {
            return "redirect:/board/product/6/";
        }

        model.addAttribute("active", "inquiry");
        model.addAttribute("products", inquiryService.findProductOptions());
        model.addAttribute("selectedProductId", productNo);
        return "community/inquiry-write";
    }

    @GetMapping("/board/product/read.html")
    public String inquiryDetail(
        @RequestParam(name = "no") long inquiryId,
        @RequestParam(name = "board_no", defaultValue = "6") int boardNo,
        HttpSession session,
        Model model
    ) {
        if (boardNo != 6) {
            return "redirect:/board/product/6/";
        }
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        model.addAttribute("active", "inquiry");
        model.addAttribute("inquiry", inquiryService.findById(inquiryId));
        model.addAttribute("canEdit", inquiryService.canEdit(loginUser, inquiryId));
        return "community/inquiry-detail";
    }

    @GetMapping("/board/product/modify.html")
    public String inquiryEdit(
        @RequestParam(name = "no") long inquiryId,
        @RequestParam(name = "board_no", defaultValue = "6") int boardNo,
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (boardNo != 6) {
            return "redirect:/board/product/6/";
        }
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/member/login.html";
        }
        if (!inquiryService.canEdit(loginUser, inquiryId)) {
            redirectAttributes.addFlashAttribute("error", "작성자만 수정할 수 있습니다.");
            return "redirect:/board/product/read.html?board_no=6&no=" + inquiryId;
        }

        var inquiry = inquiryService.findById(inquiryId);
        model.addAttribute("active", "inquiry");
        model.addAttribute("inquiry", inquiry);
        model.addAttribute("products", inquiryService.findProductOptions());
        model.addAttribute("title", inquiry.title());
        model.addAttribute("content", inquiry.content());
        model.addAttribute("selectedProductId", inquiry.productId());
        return "community/inquiry-edit";
    }

    @PostMapping("/board/product/write.html")
    public String createInquiry(
        @RequestParam(name = "productId", required = false) Long productId,
        @RequestParam String title,
        @RequestParam String content,
        HttpSession session,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/member/login.html";
        }

        try {
            inquiryService.create(loginUser, productId, title, content);
            redirectAttributes.addFlashAttribute("message", "문의가 등록되었습니다.");
            return "redirect:/board/product/6/";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("active", "inquiry");
            model.addAttribute("products", inquiryService.findProductOptions());
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("title", title);
            model.addAttribute("content", content);
            model.addAttribute("selectedProductId", productId);
            return "community/inquiry-write";
        }
    }

    @PostMapping("/board/product/modify.html")
    public String updateInquiry(
        @RequestParam(name = "no") long inquiryId,
        @RequestParam(name = "productId", required = false) Long productId,
        @RequestParam String title,
        @RequestParam String content,
        HttpSession session,
        Model model
    ) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/member/login.html";
        }

        try {
            inquiryService.update(loginUser, inquiryId, productId, title, content);
            return "redirect:/board/product/read.html?board_no=6&no=" + inquiryId;
        } catch (IllegalArgumentException ex) {
            model.addAttribute("active", "inquiry");
            model.addAttribute("inquiry", inquiryService.findById(inquiryId));
            model.addAttribute("products", inquiryService.findProductOptions());
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("title", title);
            model.addAttribute("content", content);
            model.addAttribute("selectedProductId", productId);
            return "community/inquiry-edit";
        }
    }
}
