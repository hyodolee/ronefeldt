package com.ronnefeldt.controller;

import java.util.List;
import java.util.Map;

import com.ronnefeldt.model.StoreCategory;
import com.ronnefeldt.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StoreController {

    private static final List<String> TEA_SET_BANNERS = List.of(
        "https://teehaus.co.kr/web/upload/category/shop1_24_top_561212.jpg",
        "https://teehaus.co.kr/web/upload/category/shop1_24_top_493853.jpg"
    );

    private static final Map<String, StoreCategory> STORE_CATEGORIES = Map.of(
        "tea-set", new StoreCategory("tea-set", "Tea Set", "/category/tea-set/24/", TEA_SET_BANNERS),
        "loose-tea", new StoreCategory("loose-tea", "Loose Tea", "/category/loose-tea/46/", List.of()),
        "tea-caddy", new StoreCategory("tea-caddy", "Tea-Caddy", "/category/tea-caddy/49/", List.of()),
        "leafcup", new StoreCategory("leafcup", "LeafCup", "/category/leafcup/48/", List.of()),
        "teavelope", new StoreCategory("teavelope", "Teavelope", "/category/teavelope/47/", List.of()),
        "tea-ware", new StoreCategory("tea-ware", "Tea Ware", "/category/tea-ware/26/", List.of()),
        "life-style", new StoreCategory("life-style", "Life Style", "/category/life-style/25/", List.of())
    );

    private final ProductService productService;

    public StoreController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/category/tea-set/24/")
    public String teaSet(@RequestParam(defaultValue = "1") int page, Model model) {
        return showCategory("tea-set", page, model);
    }

    @GetMapping("/category/{slug}/{categoryId}/")
    public String categoryByCafe24Path(
        @PathVariable String slug,
        @PathVariable long categoryId,
        @RequestParam(defaultValue = "1") int page,
        Model model
    ) {
        return showCategory(slug, page, model);
    }

    @GetMapping("/store/{slug}")
    public String storeCategory(
        @PathVariable String slug,
        @RequestParam(defaultValue = "1") int page,
        Model model
    ) {
        return showCategory(slug, page, model);
    }

    @GetMapping("/product/{slug}/{id}/category/{categoryId}/display/{displayId}/")
    public String productDetail(
        @PathVariable String slug,
        @PathVariable long id,
        @PathVariable long categoryId,
        @PathVariable long displayId,
        Model model
    ) {
        var product = productService.findByIdOrSlug(id, slug)
            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        var category = STORE_CATEGORIES.getOrDefault(product.categorySlug(), STORE_CATEGORIES.get("tea-set"));
        model.addAttribute("product", product);
        model.addAttribute("category", category);
        return "store/product-detail";
    }

    private String showCategory(String slug, int page, Model model) {
        StoreCategory category = STORE_CATEGORIES.getOrDefault(slug, STORE_CATEGORIES.get("tea-set"));
        var productPage = productService.findPageByCategorySlug(category.key(), page, ProductService.CATEGORY_PAGE_SIZE);
        model.addAttribute("category", category);
        model.addAttribute("products", productPage.products());
        model.addAttribute("productPage", productPage);
        return "store/category";
    }
}
