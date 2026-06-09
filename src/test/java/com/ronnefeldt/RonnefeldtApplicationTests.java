package com.ronnefeldt;

import com.ronnefeldt.model.LoginUser;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RonnefeldtApplicationTests {

    private final MockMvcTester mvc;
    private final MockMvc mockMvc;

    @Autowired
    RonnefeldtApplicationTests(MockMvcTester mvc, MockMvc mockMvc) {
        this.mvc = mvc;
        this.mockMvc = mockMvc;
    }

    @Test
    void contextLoads() {
    }

    @Test
    void indexPageLoads() {
        assertThat(mvc.get().uri("/"))
            .hasStatusOk()
            .bodyText()
            .contains("id=\"header\"", "Ronnefeldt", "/video/tea.mp4", "video-bg",
                "teaVideo", "videoPlayToggle", "videoSoundToggle", "video-side-btn");
    }

    @Test
    void teaSetPageLoads() {
        assertThat(mvc.get().uri("/category/tea-set/24/"))
            .hasStatusOk()
            .bodyText()
            .contains("Tea Set", "Rich Aroma LeafCup", "Week Focus LeafCup", "Healthy Week LeafCup", "25,000원",
                "Search", "Cart (", ">0</span>", "prd-actions", "data-view=\"list\"", "data-view=\"grid\"", "product-summary");
    }

    @Test
    void storeCategoryLayoutCanRenderOtherMenus() {
        assertThat(mvc.get().uri("/store/loose-tea"))
            .hasStatusOk()
            .bodyText()
            .contains("Loose Tea", "category-visual", "product-grid");
    }

    @Test
    void allStoreSubCategoryPagesLoad() {
        Map<String, String> categories = Map.of(
            "/category/loose-tea/46/", "아쌈 바리 Loose Tea / 250g",
            "/category/tea-caddy/49/", "아쌈 바리 아이리시 브렉퍼스트 TeaCaddy®",
            "/category/leafcup/48/", "다즐링 썸머 골드 Leafcup®",
            "/category/teavelope/47/", "코파 카바나 Teavelope®",
            "/category/tea-ware/26/", "Tee Tumbler",
            "/category/life-style/25/", "Ronnefeldt  Weekend Bag"
        );

        categories.forEach((path, productName) -> assertThat(mvc.get().uri(path))
            .hasStatusOk()
            .bodyText()
            .contains("category-visual", "product-grid", "product-card", productName));
    }

    @Test
    void storeSubCategoryUsesPlainLayoutAndPagination() {
        assertThat(mvc.get().uri("/category/loose-tea/46/"))
            .hasStatusOk()
            .bodyText()
            .contains("no-banner", "32</strong>", "page-numbers", "?page=2", "?page=3")
            .doesNotContain("category-banner");

        assertThat(mvc.get().uri("/category/loose-tea/46/?page=2"))
            .hasStatusOk()
            .bodyText()
            .contains("no-banner", "칠 아웃 위드 허브 Loose Tea / 100g", "?page=1", "?page=3")
            .doesNotContain("아쌈 바리 Loose Tea / 250g");
    }

    @Test
    void teaSetStillUsesVisualBanner() {
        assertThat(mvc.get().uri("/category/tea-set/24/"))
            .hasStatusOk()
            .bodyText()
            .contains("has-banner", "category-banner", "shop1_24_top");
    }

    @Test
    void productDetailPageLoads() {
        assertThat(mvc.get().uri("/product/rich-aroma-leafcup/18/category/24/display/1/"))
            .hasStatusOk()
            .bodyText()
            .contains("Rich Aroma LeafCup", "cart-add-form", "/cart/items", "DETAIL", "GUIDE", "REVIEW", "Q &amp; A");
    }

    @Test
    void addingCartItemRequiresLogin() throws Exception {
        mockMvc.perform(post("/cart/items")
                .param("productId", "18")
                .param("quantity", "1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("/member/login.html"));
    }

    @Test
    void headerCartCountReflectsLoggedInCartItems() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", new LoginUser("naver", "header-cart-user", "cart@example.com", "", "cart-user"));

        mockMvc.perform(post("/cart/items")
                .session(session)
                .param("productId", "18")
                .param("quantity", "1"))
            .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/order/basket.html").session(session))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Cart ( <span>1</span> )")));
    }

    @Test
    void actuatorHealthEndpointLoads() {
        assertThat(mvc.get().uri("/actuator/health"))
            .hasStatusOk()
            .bodyText()
            .contains("\"status\":\"UP\"");
    }

    @Test
    void loginPageLoads() {
        assertThat(mvc.get().uri("/member/login.html"))
            .hasStatusOk()
            .bodyText()
            .contains("login-panel", "memberId", "memberPassword", "naver-login", "Search", "Cart (", ">0</span>");
    }

    @Test
    void joinPageLoads() {
        assertThat(mvc.get().uri("/member/join.html"))
            .hasStatusOk()
            .bodyText()
            .contains("join-panel", "joinMemberId", "joinPassword", "joinEmail", "join-naver", "/oauth/naver/login");
    }

    @Test
    void basketPageLoads() {
        assertThat(mvc.get().uri("/order/basket.html"))
            .hasStatusOk()
            .bodyText()
            .contains("basket-panel", "basket-empty", "basket-actions", "guide-panel", "basket-tabs");
    }

    @Test
    void basketPageSupportsOverseasDeliveryTab() {
        assertThat(mvc.get().uri("/order/basket.html?delvtype=B"))
            .hasStatusOk()
            .bodyText()
            .contains("delvtype=B", "role=\"tab\"", "active");
    }

    @Test
    void basketPageShowsBenefitBoxWhenUserIsLoggedIn() throws Exception {
        mockMvc.perform(get("/order/basket.html")
                .sessionAttr("loginUser", new LoginUser("naver", "naver-id", "member@example.com", "", "tester")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("basket-benefit")))
            .andExpect(content().string(containsString("basket-tabs")));
    }

    @Test
    void myshopRedirectsToLoginWhenUserIsAnonymous() throws Exception {
        mockMvc.perform(get("/myshop/index.html"))
            .andExpect(status().is3xxRedirection())
            .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("/member/login.html"));
    }

    @Test
    void myshopPageLoadsWhenUserIsLoggedIn() throws Exception {
        mockMvc.perform(get("/myshop/index.html")
                .sessionAttr("loginUser", new LoginUser("naver", "myshop-id", "member@example.com", "", "tester")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("myshop-panel")))
            .andExpect(content().string(containsString("myshop-bankbook")))
            .andExpect(content().string(containsString("myshop-order-state")))
            .andExpect(content().string(containsString("myshop-menu-grid")))
            .andExpect(content().string(containsString("Wishlist")))
            .andExpect(content().string(containsString("/myshop/index.html")));
    }

    @Test
    void sideMenuShowsLogoutWhenUserIsLoggedIn() throws Exception {
        mockMvc.perform(get("/").sessionAttr("loginUser", new LoginUser("naver", "naver-id", "member@example.com", "", "tester")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("tester")))
            .andExpect(content().string(containsString("&#47196;&#44536;&#50500;&#50883;")))
            .andExpect(content().string(containsString("/myshop/index.html")));
    }

    @Test
    void inquiryListPageLoads() {
        assertThat(mvc.get().uri("/board/product/6/"))
            .hasStatusOk()
            .bodyText()
            .contains("문의하기", "게시물이 없습니다.", "글쓰기", "board/product/write.html");
    }

    @Test
    void inquiryWriteRequiresLogin() throws Exception {
        mockMvc.perform(get("/board/product/write.html").param("board_no", "6"))
            .andExpect(status().is3xxRedirection())
            .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("/member/login.html"));
    }

    @Test
    void loggedInUserCanCreateInquiry() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", new LoginUser("naver", "inquiry-user", "inquiry@example.com", "문의회원", "문의회원"));

        mockMvc.perform(post("/board/product/write.html")
                .session(session)
                .param("board_no", "6")
                .param("productId", "18")
                .param("title", "배송 문의입니다")
                .param("content", "언제 배송되나요?"))
            .andExpect(status().is3xxRedirection())
            .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("/board/product/6/"));

        mockMvc.perform(get("/board/product/6/").session(session))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("배송 문의입니다")))
            .andExpect(content().string(containsString("read.html")))
            .andExpect(content().string(containsString("Rich Aroma LeafCup")))
            .andExpect(content().string(containsString("답변대기")));
    }

    @Test
    void inquiryDetailAndEditWorkForOwner() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", new LoginUser("naver", "inquiry-owner", "owner@example.com", "문의작성자", "문의작성자"));

        mockMvc.perform(post("/board/product/write.html")
                .session(session)
                .param("board_no", "6")
                .param("productId", "18")
                .param("title", "수정 전 문의")
                .param("content", "수정 전 내용"))
            .andExpect(status().is3xxRedirection());

        String list = mockMvc.perform(get("/board/product/6/").session(session))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        var matcher = Pattern.compile("read\\.html[^\"']*no=([0-9]+)").matcher(list);
        assertThat(matcher.find()).isTrue();
        String id = matcher.group(1);

        mockMvc.perform(get("/board/product/read.html").session(session).param("board_no", "6").param("no", id))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("수정 전 문의")))
            .andExpect(content().string(containsString("수정")));

        mockMvc.perform(post("/board/product/modify.html")
                .session(session)
                .param("board_no", "6")
                .param("no", id)
                .param("productId", "16")
                .param("title", "수정 후 문의")
                .param("content", "수정 후 내용"))
            .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/board/product/read.html").session(session).param("board_no", "6").param("no", id))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("수정 후 문의")))
            .andExpect(content().string(containsString("Week Focus LeafCup")));
    }

    @Test
    void localMemberCanJoinAndLogin() throws Exception {
        mockMvc.perform(post("/member/join.html")
                .param("memberId", "localuser1")
                .param("password", "password1234")
                .param("passwordConfirm", "password1234")
                .param("name", "로컬회원")
                .param("email", "localuser1@example.com")
                .param("agreeTerms", "true"))
            .andExpect(status().is3xxRedirection())
            .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("/"));

        MockHttpSession loginSession = new MockHttpSession();
        mockMvc.perform(post("/member/login.html")
                .session(loginSession)
                .param("memberId", "localuser1")
                .param("memberPassword", "password1234"))
            .andExpect(status().is3xxRedirection())
            .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("/"));

        mockMvc.perform(get("/myshop/index.html").session(loginSession))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("myshop-panel")));
    }

    @Test
    void naverLoginShowsConfigurationMessageWhenClientIdIsMissing() {
        assertThat(mvc.get().uri("/oauth/naver/not-configured"))
            .hasStatusOk()
            .bodyText()
            .contains("Naver OAuth is not configured", "NAVER_CLIENT_ID", "NAVER_CLIENT_SECRET");
    }

    @Test
    void naverLoginRedirectsToConfigurationMessageWhenCredentialsAreMissing() {
        assertThat(mvc.get().uri("/oauth/naver/login"))
            .hasStatus3xxRedirection()
            .hasRedirectedUrl("/oauth/naver/not-configured");
    }
}
