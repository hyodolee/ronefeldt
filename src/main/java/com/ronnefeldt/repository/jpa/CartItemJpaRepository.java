package com.ronnefeldt.repository.jpa;

import java.util.List;
import java.util.Optional;

import com.ronnefeldt.entity.CartEntity;
import com.ronnefeldt.entity.CartItemEntity;
import com.ronnefeldt.entity.ProductEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemJpaRepository extends JpaRepository<CartItemEntity, Long> {

    @EntityGraph(attributePaths = {"cart", "cart.member", "product"})
    List<CartItemEntity> findByCartMemberProviderAndCartMemberProviderUserIdAndDeliveryTypeOrderByIdDesc(
        String provider,
        String providerUserId,
        String deliveryType
    );

    Optional<CartItemEntity> findByCartAndProductAndDeliveryType(CartEntity cart, ProductEntity product, String deliveryType);

    Optional<CartItemEntity> findByIdAndCartMemberProviderAndCartMemberProviderUserId(
        Long id,
        String provider,
        String providerUserId
    );

    @Query("""
        select coalesce(sum(item.quantity), 0)
        from CartItemEntity item
        where item.cart.member.provider = :provider
          and item.cart.member.providerUserId = :providerUserId
        """)
    int sumQuantityByMember(
        @Param("provider") String provider,
        @Param("providerUserId") String providerUserId
    );
}
