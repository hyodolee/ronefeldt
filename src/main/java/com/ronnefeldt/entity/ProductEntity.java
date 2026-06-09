package com.ronnefeldt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int price;

    @Column(name = "main_image_url", length = 1000)
    private String mainImageUrl;

    @Column(name = "detail_image_urls", columnDefinition = "TEXT")
    private String detailImageUrls;

    @Column(name = "search_keywords", columnDefinition = "TEXT")
    private String searchKeywords;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "is_sold_out", nullable = false)
    private boolean soldOut;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected ProductEntity() {
    }

    public Long getId() {
        return id;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    public String getMainImageUrl() {
        return mainImageUrl;
    }

    public String getStatus() {
        return status;
    }

    public boolean isSoldOut() {
        return soldOut;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
