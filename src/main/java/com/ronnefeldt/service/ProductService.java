package com.ronnefeldt.service;

import java.util.List;
import java.util.Optional;

import com.ronnefeldt.entity.ProductEntity;
import com.ronnefeldt.model.Product;
import com.ronnefeldt.model.ProductPage;
import com.ronnefeldt.repository.jpa.ProductJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    public static final int CATEGORY_PAGE_SIZE = 12;

    private final ProductJpaRepository productJpaRepository;

    public ProductService(ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    public List<Product> findByCategorySlug(String slug) {
        return findPageByCategorySlug(slug, 1, Integer.MAX_VALUE).products();
    }

    public ProductPage findPageByCategorySlug(String slug, int requestedPage, int size) {
        int safeSize = Math.max(1, size);
        long totalElements = productJpaRepository.countVisibleByCategorySlug(slug);
        int totalPages = totalElements == 0 ? 1 : (int) Math.ceil((double) totalElements / safeSize);
        int safePage = Math.min(Math.max(1, requestedPage), totalPages);
        Pageable pageable = PageRequest.of(safePage - 1, safeSize);
        List<Product> products = productJpaRepository.findVisibleByCategorySlug(slug, pageable).stream()
            .map(this::toModel)
            .toList();

        return new ProductPage(products, totalElements, safePage, safeSize, totalPages);
    }

    public Optional<Product> findByIdOrSlug(long id, String slug) {
        return productJpaRepository.findWithCategoryById(id)
            .map(this::toModel)
            .or(() -> findBySlugFallback(slug));
    }

    private Optional<Product> findBySlugFallback(String slug) {
        return productJpaRepository.findAll().stream()
            .map(this::toModel)
            .filter(product -> product.slug().equals(slug))
            .findFirst();
    }

    private Product toModel(ProductEntity entity) {
        var category = entity.getCategory();
        return new Product(
            entity.getId(),
            category.getId(),
            category.getSlug(),
            category.getName(),
            entity.getName(),
            entity.getSummary(),
            entity.getDescription(),
            entity.getPrice(),
            entity.getMainImageUrl(),
            entity.isSoldOut(),
            entity.getDisplayOrder()
        );
    }
}
