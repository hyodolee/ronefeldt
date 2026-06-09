package com.ronnefeldt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "members",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_members_provider_user", columnNames = {"provider", "provider_user_id"})
    }
)
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 100)
    private String name;

    @Column(length = 100)
    private String nickname;

    @Column(length = 30)
    private String provider;

    @Column(name = "provider_user_id", length = 255)
    private String providerUserId;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 30)
    private String grade = "BRONZE";

    @Column(nullable = false)
    private int points;

    @Column(nullable = false, length = 30)
    private String role = "USER";

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE";

    protected MemberEntity() {
    }

    public MemberEntity(String email, String name, String nickname, String provider, String providerUserId) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.provider = provider;
        this.providerUserId = providerUserId;
    }

    public MemberEntity(String email, String name, String nickname, String provider, String providerUserId, String passwordHash) {
        this(email, name, nickname, provider, providerUserId);
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
