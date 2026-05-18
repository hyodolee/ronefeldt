package com.ronnefeldt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class RonnefeldtApplicationTests {

    private final MockMvcTester mvc;

    @Autowired
    RonnefeldtApplicationTests(MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @Test
    void contextLoads() {
    }

    @Test
    void indexPageLoads() {
        assertThat(mvc.get().uri("/"))
            .hasStatusOk()
            .bodyText()
            .contains("id=\"header\"", "Ronnefeldt");
    }

    @Test
    void teaSetPageLoads() {
        assertThat(mvc.get().uri("/category/tea-set/24/"))
            .hasStatusOk()
            .bodyText()
            .contains("Tea Set", "Rich Aroma LeafCup", "Week Focus LeafCup", "Healthy Week LeafCup", "25,000\uC6D0",
                "Search", "Cart ( 0 )", "prd-actions", "data-view=\"list\"", "data-view=\"grid\"", "product-summary",
                "&#44060;&#51032;");
    }

    @Test
    void storeCategoryLayoutCanRenderOtherMenus() {
        assertThat(mvc.get().uri("/store/loose-tea"))
            .hasStatusOk()
            .bodyText()
            .contains("Loose Tea", "category-visual", "product-grid");
    }
}
