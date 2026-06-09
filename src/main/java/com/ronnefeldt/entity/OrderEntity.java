package com.ronnefeldt.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(name = "order_no", nullable = false, length = 60)
    private String orderNo;

    @Column(name = "order_status", nullable = false, length = 30)
    private String orderStatus = "CREATED";

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();

    protected OrderEntity() {
    }

    public OrderEntity(MemberEntity member, String orderNo, int totalAmount) {
        this.member = member;
        this.orderNo = orderNo;
        this.totalAmount = totalAmount;
    }

    public void addItem(OrderItemEntity item) {
        items.add(item);
        item.assignOrder(this);
    }

    public void markPaid() {
        this.orderStatus = "PAID";
    }

    public Long getId() {
        return id;
    }

    public MemberEntity getMember() {
        return member;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public List<OrderItemEntity> getItems() {
        return items;
    }
}
