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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "cart_items",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_cart_items_cart_product_delivery", columnNames = {"cart_id", "product_id", "delivery_type"})
    }
)
public class CartItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private CartEntity cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false)
    private int unitPrice;

    @Column(name = "delivery_type", nullable = false, length = 30)
    private String deliveryType;

    protected CartItemEntity() {
    }

    public CartItemEntity(CartEntity cart, ProductEntity product, int quantity, int unitPrice, String deliveryType) {
        this.cart = cart;
        this.product = product;
        this.quantity = Math.max(quantity, 1);
        this.unitPrice = unitPrice;
        this.deliveryType = deliveryType;
    }

    public Long getId() {
        return id;
    }

    public CartEntity getCart() {
        return cart;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void addQuantity(int quantity) {
        this.quantity += Math.max(quantity, 1);
        this.unitPrice = product.getPrice();
    }

    public void changeQuantity(int quantity) {
        this.quantity = Math.max(quantity, 1);
    }
}
