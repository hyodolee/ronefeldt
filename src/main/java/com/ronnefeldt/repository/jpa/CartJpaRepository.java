package com.ronnefeldt.repository.jpa;

import java.util.Optional;

import com.ronnefeldt.entity.CartEntity;
import com.ronnefeldt.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartJpaRepository extends JpaRepository<CartEntity, Long> {

    Optional<CartEntity> findByMember(MemberEntity member);
}
