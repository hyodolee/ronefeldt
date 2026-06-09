package com.ronnefeldt.repository.jpa;

import java.util.Optional;

import com.ronnefeldt.entity.OrderEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

    @EntityGraph(attributePaths = {"items", "items.product", "member"})
    Optional<OrderEntity> findByIdAndMemberProviderAndMemberProviderUserId(
        Long id,
        String provider,
        String providerUserId
    );
}
