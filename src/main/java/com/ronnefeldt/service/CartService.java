package com.ronnefeldt.service;

import java.util.List;
import java.util.Locale;

import com.ronnefeldt.entity.CartEntity;
import com.ronnefeldt.entity.CartItemEntity;
import com.ronnefeldt.entity.MemberEntity;
import com.ronnefeldt.entity.ProductEntity;
import com.ronnefeldt.model.CartItem;
import com.ronnefeldt.model.LoginUser;
import com.ronnefeldt.repository.jpa.CartItemJpaRepository;
import com.ronnefeldt.repository.jpa.CartJpaRepository;
import com.ronnefeldt.repository.jpa.MemberJpaRepository;
import com.ronnefeldt.repository.jpa.ProductJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final MemberJpaRepository memberJpaRepository;
    private final CartJpaRepository cartJpaRepository;
    private final CartItemJpaRepository cartItemJpaRepository;
    private final ProductJpaRepository productJpaRepository;

    public CartService(
        MemberJpaRepository memberJpaRepository,
        CartJpaRepository cartJpaRepository,
        CartItemJpaRepository cartItemJpaRepository,
        ProductJpaRepository productJpaRepository
    ) {
        this.memberJpaRepository = memberJpaRepository;
        this.cartJpaRepository = cartJpaRepository;
        this.cartItemJpaRepository = cartItemJpaRepository;
        this.productJpaRepository = productJpaRepository;
    }

    @Transactional
    public void addItem(LoginUser loginUser, long productId, int quantity, String deliveryType) {
        String normalizedDeliveryType = normalizeDeliveryType(deliveryType);
        MemberEntity member = getOrCreateMember(loginUser);
        CartEntity cart = cartJpaRepository.findByMember(member)
            .orElseGet(() -> cartJpaRepository.save(new CartEntity(member)));
        ProductEntity product = productJpaRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. productId=" + productId));

        if (product.isSoldOut()) {
            throw new IllegalArgumentException("품절 상품은 장바구니에 담을 수 없습니다. productId=" + productId);
        }

        cartItemJpaRepository.findByCartAndProductAndDeliveryType(cart, product, normalizedDeliveryType)
            .ifPresentOrElse(
                item -> item.addQuantity(quantity),
                () -> cartItemJpaRepository.save(new CartItemEntity(cart, product, quantity, product.getPrice(), normalizedDeliveryType))
            );
    }

    @Transactional(readOnly = true)
    public List<CartItem> findItems(LoginUser loginUser, String deliveryType) {
        if (loginUser == null) {
            return List.of();
        }

        return cartItemJpaRepository
            .findByCartMemberProviderAndCartMemberProviderUserIdAndDeliveryTypeOrderByIdDesc(
                normalizeProvider(loginUser.provider()),
                loginUser.providerId(),
                normalizeDeliveryType(deliveryType)
            )
            .stream()
            .map(this::toModel)
            .toList();
    }

    @Transactional(readOnly = true)
    public int findTotalQuantity(LoginUser loginUser) {
        if (loginUser == null) {
            return 0;
        }

        return cartItemJpaRepository.sumQuantityByMember(
            normalizeProvider(loginUser.provider()),
            loginUser.providerId()
        );
    }

    @Transactional
    public void updateQuantity(LoginUser loginUser, long cartItemId, int quantity) {
        CartItemEntity item = findOwnedItem(loginUser, cartItemId);
        item.changeQuantity(quantity);
    }

    @Transactional
    public void deleteItem(LoginUser loginUser, long cartItemId) {
        CartItemEntity item = findOwnedItem(loginUser, cartItemId);
        cartItemJpaRepository.delete(item);
    }

    private MemberEntity getOrCreateMember(LoginUser loginUser) {
        String provider = normalizeProvider(loginUser.provider());
        String providerUserId = loginUser.providerId();
        return memberJpaRepository.findByProviderAndProviderUserId(provider, providerUserId)
            .orElseGet(() -> memberJpaRepository.save(new MemberEntity(
                blankToFallback(loginUser.email(), providerUserId + "@naver.local"),
                blankToFallback(loginUser.name(), loginUser.displayName()),
                blankToFallback(loginUser.nickname(), loginUser.displayName()),
                provider,
                providerUserId
            )));
    }

    private CartItemEntity findOwnedItem(LoginUser loginUser, long cartItemId) {
        return cartItemJpaRepository.findByIdAndCartMemberProviderAndCartMemberProviderUserId(
                cartItemId,
                normalizeProvider(loginUser.provider()),
                loginUser.providerId()
            )
            .orElseThrow(() -> new IllegalArgumentException("장바구니 상품을 찾을 수 없습니다."));
    }

    private CartItem toModel(CartItemEntity entity) {
        ProductEntity product = entity.getProduct();
        return new CartItem(
            entity.getId(),
            product.getId(),
            product.getName(),
            displaySummary(product.getSummary()),
            displayImageUrl(product.getName(), product.getMainImageUrl()),
            entity.getQuantity(),
            entity.getUnitPrice(),
            entity.getDeliveryType()
        );
    }

    private String normalizeDeliveryType(String deliveryType) {
        return "B".equalsIgnoreCase(deliveryType) || "OVERSEAS".equalsIgnoreCase(deliveryType) ? "OVERSEAS" : "DOMESTIC";
    }

    private String normalizeProvider(String provider) {
        return provider == null || provider.isBlank() ? "LOCAL" : provider.toUpperCase(Locale.ROOT);
    }

    private String displaySummary(String summary) {
        if (summary == null || summary.isBlank() || summary.contains("?")) {
            return "단독구매상품";
        }
        return summary;
    }

    private String displayImageUrl(String name, String imageUrl) {
        return switch (name) {
            case "Rich Aroma LeafCup" -> "https://teehaus.co.kr/web/product/big/202503/1ae4f5970b2537b3dd57f97ac423e812.jpg";
            case "Week Focus LeafCup" -> "https://teehaus.co.kr/web/product/big/202503/dcfe96872a82ba911970d3514ad51b2b.jpg";
            case "Healthy Week LeafCup" -> "https://teehaus.co.kr/web/product/big/202503/5e668feba8e3727c596c5bacff1777e2.jpg";
            default -> imageUrl;
        };
    }

    private String blankToFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
