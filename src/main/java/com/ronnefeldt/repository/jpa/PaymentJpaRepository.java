package com.ronnefeldt.repository.jpa;

import java.util.Optional;

import com.ronnefeldt.entity.OrderEntity;
import com.ronnefeldt.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByOrder(OrderEntity order);
}
