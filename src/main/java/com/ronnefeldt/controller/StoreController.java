package com.ronnefeldt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class StoreController {

    private static final List<String> TEA_SET_BANNERS = List.of(
        "https://teehaus.co.kr/web/upload/category/shop1_24_top_561212.jpg",
        "https://teehaus.co.kr/web/upload/category/shop1_24_top_493853.jpg"
    );

    private static final Map<String, StoreCategory> STORE_CATEGORIES = Map.of(
        "tea-set", new StoreCategory(
            "tea-set",
            "Tea Set",
            "/category/tea-set/24/",
            TEA_SET_BANNERS,
            List.of(
                new TeaProduct(
                    "Rich Aroma LeafCup",
                    "\uB2E8\uB3C5\uAD6C\uB9E4\uC0C1\uD488",
                    "25,000\uC6D0",
                    "https://teehaus.co.kr/web/product/medium/202503/d06664755e02edd978e71168f7af3a66.jpg",
                    "https://teehaus.co.kr/product/rich-aroma-leafcup/18/category/24/display/1/"
                ),
                new TeaProduct(
                    "Week Focus LeafCup",
                    "\uB2E8\uB3C5\uAD6C\uB9E4\uC0C1\uD488",
                    "25,000\uC6D0",
                    "https://teehaus.co.kr/web/product/medium/202503/46114c70e4865d5c89226c3b74ee436e.jpg",
                    "https://teehaus.co.kr/product/week-focus-leafcup/16/category/24/display/1/"
                ),
                new TeaProduct(
                    "Healthy Week LeafCup",
                    "\uB2E8\uB3C5\uAD6C\uB9E4\uC0C1\uD488",
                    "25,000\uC6D0",
                    "https://teehaus.co.kr/web/product/medium/202503/1cb9e1581adf882dd9714037a5fe7e2a.jpg",
                    "https://teehaus.co.kr/product/healthy-week-leafcup/15/category/24/display/1/"
                )
            )
        ),
        "loose-tea", emptyCategory("loose-tea", "Loose Tea", "/store/loose-tea"),
        "tea-caddy", emptyCategory("tea-caddy", "Tea-Caddy", "/store/tea-caddy"),
        "leafcup", emptyCategory("leafcup", "LeafCup", "/store/leafcup"),
        "teavelope", emptyCategory("teavelope", "Teavelope", "/store/teavelope"),
        "tea-ware", emptyCategory("tea-ware", "Tea Ware", "/store/tea-ware"),
        "life-style", emptyCategory("life-style", "Life Style", "/store/life-style")
    );

    @GetMapping("/category/tea-set/24/")
    public String teaSet(Model model) {
        return showCategory("tea-set", model);
    }

    @GetMapping("/store/{slug}")
    public String storeCategory(@PathVariable String slug, Model model) {
        return showCategory(slug, model);
    }

    private String showCategory(String slug, Model model) {
        StoreCategory category = STORE_CATEGORIES.getOrDefault(slug, STORE_CATEGORIES.get("tea-set"));
        model.addAttribute("category", category);
        model.addAttribute("products", category.products());
        return "store/category";
    }

    private static StoreCategory emptyCategory(String key, String title, String path) {
        return new StoreCategory(key, title, path, TEA_SET_BANNERS, List.of());
    }

    public record StoreCategory(String key, String title, String path, List<String> bannerImages, List<TeaProduct> products) {
    }

    public record TeaProduct(String name, String summary, String price, String imageUrl, String detailUrl) {
    }
}
