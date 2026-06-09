package com.ronnefeldt.repository.jpa;

import java.util.Optional;

import com.ronnefeldt.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<MemberEntity, Long> {

    Optional<MemberEntity> findByProviderAndProviderUserId(String provider, String providerUserId);

    Optional<MemberEntity> findByEmail(String email);

    boolean existsByProviderAndProviderUserId(String provider, String providerUserId);

    boolean existsByEmail(String email);
}
