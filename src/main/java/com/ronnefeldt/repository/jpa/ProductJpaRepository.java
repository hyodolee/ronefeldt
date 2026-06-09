package com.ronnefeldt.repository.jpa;

import java.util.List;
import java.util.Optional;

import com.ronnefeldt.entity.ProductEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    @EntityGraph(attributePaths = "category")
    @Query("""
        select p
          from ProductEntity p
          join p.category c
         where c.slug = :slug
           and p.status <> 'HIDDEN'
         order by p.displayOrder asc, p.id asc
        """)
    List<ProductEntity> findVisibleByCategorySlug(@Param("slug") String slug);

    @EntityGraph(attributePaths = "category")
    @Query("""
        select p
          from ProductEntity p
          join p.category c
         where c.slug = :slug
           and p.status <> 'HIDDEN'
         order by p.displayOrder asc, p.id asc
        """)
    List<ProductEntity> findVisibleByCategorySlug(@Param("slug") String slug, Pageable pageable);

    @Query("""
        select count(p)
          from ProductEntity p
          join p.category c
         where c.slug = :slug
           and p.status <> 'HIDDEN'
        """)
    long countVisibleByCategorySlug(@Param("slug") String slug);

    @EntityGraph(attributePaths = "category")
    Optional<ProductEntity> findWithCategoryById(Long id);
}
