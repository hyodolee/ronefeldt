package com.ronnefeldt.service;

import java.util.List;
import java.util.Locale;

import com.ronnefeldt.entity.InquiryEntity;
import com.ronnefeldt.entity.MemberEntity;
import com.ronnefeldt.entity.ProductEntity;
import com.ronnefeldt.model.Inquiry;
import com.ronnefeldt.model.LoginUser;
import com.ronnefeldt.model.Product;
import com.ronnefeldt.repository.jpa.InquiryJpaRepository;
import com.ronnefeldt.repository.jpa.MemberJpaRepository;
import com.ronnefeldt.repository.jpa.ProductJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InquiryService {

    private final InquiryJpaRepository inquiryJpaRepository;
    private final MemberJpaRepository memberJpaRepository;
    private final ProductJpaRepository productJpaRepository;

    public InquiryService(
        InquiryJpaRepository inquiryJpaRepository,
        MemberJpaRepository memberJpaRepository,
        ProductJpaRepository productJpaRepository
    ) {
        this.inquiryJpaRepository = inquiryJpaRepository;
        this.memberJpaRepository = memberJpaRepository;
        this.productJpaRepository = productJpaRepository;
    }

    @Transactional(readOnly = true)
    public List<Inquiry> findAll() {
        return inquiryJpaRepository.findAllByOrderByIdDesc().stream()
            .map(this::toModel)
            .toList();
    }

    @Transactional(readOnly = true)
    public Inquiry findById(long id) {
        return inquiryJpaRepository.findWithMemberAndProductById(id)
            .map(this::toModel)
            .orElseThrow(() -> new IllegalArgumentException("문의글을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public boolean canEdit(LoginUser loginUser, long inquiryId) {
        if (loginUser == null) {
            return false;
        }
        return inquiryJpaRepository.findWithMemberAndProductById(inquiryId)
            .map(inquiry -> isOwner(loginUser, inquiry.getMember()))
            .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<Product> findProductOptions() {
        return productJpaRepository.findAll().stream()
            .filter(product -> !"HIDDEN".equalsIgnoreCase(product.getStatus()))
            .sorted((left, right) -> {
                int categoryCompare = Long.compare(left.getCategory().getId(), right.getCategory().getId());
                if (categoryCompare != 0) {
                    return categoryCompare;
                }
                int orderCompare = Integer.compare(left.getDisplayOrder(), right.getDisplayOrder());
                return orderCompare != 0 ? orderCompare : Long.compare(left.getId(), right.getId());
            })
            .map(this::toProductModel)
            .toList();
    }

    @Transactional
    public Inquiry create(LoginUser loginUser, Long productId, String title, String content) {
        if (loginUser == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        String cleanTitle = trimToLimit(title, 200);
        String cleanContent = content == null ? "" : content.trim();
        if (cleanTitle.isBlank()) {
            throw new IllegalArgumentException("제목을 입력해 주세요.");
        }
        if (cleanContent.isBlank()) {
            throw new IllegalArgumentException("문의 내용을 입력해 주세요.");
        }

        MemberEntity member = getOrCreateMember(loginUser);
        ProductEntity product = productId == null
            ? null
            : productJpaRepository.findById(productId).orElse(null);
        InquiryEntity inquiry = inquiryJpaRepository.save(new InquiryEntity(member, product, cleanTitle, cleanContent));
        return toModel(inquiry);
    }

    @Transactional
    public Inquiry update(LoginUser loginUser, long inquiryId, Long productId, String title, String content) {
        if (loginUser == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        String cleanTitle = trimToLimit(title, 200);
        String cleanContent = content == null ? "" : content.trim();
        if (cleanTitle.isBlank()) {
            throw new IllegalArgumentException("제목을 입력해 주세요.");
        }
        if (cleanContent.isBlank()) {
            throw new IllegalArgumentException("문의 내용을 입력해 주세요.");
        }

        InquiryEntity inquiry = inquiryJpaRepository.findWithMemberAndProductById(inquiryId)
            .orElseThrow(() -> new IllegalArgumentException("문의글을 찾을 수 없습니다."));
        if (!isOwner(loginUser, inquiry.getMember())) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        ProductEntity product = productId == null
            ? null
            : productJpaRepository.findById(productId).orElse(null);
        inquiry.update(cleanTitle, cleanContent, product);
        return toModel(inquiry);
    }

    private MemberEntity getOrCreateMember(LoginUser loginUser) {
        String provider = normalizeProvider(loginUser.provider());
        String providerUserId = loginUser.providerId();
        return memberJpaRepository.findByProviderAndProviderUserId(provider, providerUserId)
            .orElseGet(() -> memberJpaRepository.save(new MemberEntity(
                blankToFallback(loginUser.email(), providerUserId + "@naver.local"),
                blankToFallback(loginUser.name(), loginUser.displayName()),
                blankToFallback(loginUser.nickname(), loginUser.displayName()),
                provider,
                providerUserId
            )));
    }

    private Inquiry toModel(InquiryEntity entity) {
        MemberEntity member = entity.getMember();
        ProductEntity product = entity.getProduct();
        boolean answered = "ANSWERED".equalsIgnoreCase(entity.getStatus())
            || (entity.getAnswer() != null && !entity.getAnswer().isBlank());
        return new Inquiry(
            entity.getId(),
            entity.getTitle(),
            entity.getContent(),
            displayWriter(member),
            product == null ? null : product.getId(),
            product == null ? "" : product.getName(),
            entity.getStatus(),
            entity.getCreatedAt(),
            answered
        );
    }

    private Product toProductModel(ProductEntity entity) {
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

    private String displayWriter(MemberEntity member) {
        if (member.getNickname() != null && !member.getNickname().isBlank()) {
            return member.getNickname();
        }
        if (member.getName() != null && !member.getName().isBlank()) {
            return member.getName();
        }
        if (member.getEmail() != null && !member.getEmail().isBlank()) {
            int at = member.getEmail().indexOf('@');
            return at > 1 ? member.getEmail().substring(0, at) : member.getEmail();
        }
        return "회원";
    }

    private String normalizeProvider(String provider) {
        return provider == null || provider.isBlank() ? "LOCAL" : provider.toUpperCase(Locale.ROOT);
    }

    private boolean isOwner(LoginUser loginUser, MemberEntity member) {
        return normalizeProvider(loginUser.provider()).equalsIgnoreCase(normalizeProvider(member.getProvider()))
            && loginUser.providerId() != null
            && loginUser.providerId().equals(member.getProviderUserId());
    }

    private String blankToFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String trimToLimit(String value, int maxLength) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
