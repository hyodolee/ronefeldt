package com.ronnefeldt.repository.jpa;

import java.util.List;
import java.util.Optional;

import com.ronnefeldt.entity.InquiryEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InquiryJpaRepository extends JpaRepository<InquiryEntity, Long> {

    @EntityGraph(attributePaths = {"member", "product"})
    List<InquiryEntity> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = {"member", "product"})
    @Query("select i from InquiryEntity i where i.id = :id")
    Optional<InquiryEntity> findWithMemberAndProductById(@Param("id") Long id);
}
