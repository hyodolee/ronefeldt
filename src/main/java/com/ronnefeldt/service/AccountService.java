package com.ronnefeldt.service;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.ronnefeldt.entity.MemberEntity;
import com.ronnefeldt.model.LoginUser;
import com.ronnefeldt.repository.jpa.MemberJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private static final String LOCAL_PROVIDER = "LOCAL";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;

    private final MemberJpaRepository memberJpaRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public AccountService(MemberJpaRepository memberJpaRepository) {
        this.memberJpaRepository = memberJpaRepository;
    }

    @Transactional
    public LoginUser join(String memberId, String password, String passwordConfirm, String name, String email, boolean agreeTerms) {
        String cleanMemberId = require(memberId, "아이디를 입력해 주세요.").trim();
        String cleanName = require(name, "이름을 입력해 주세요.").trim();
        String cleanEmail = require(email, "이메일을 입력해 주세요.").trim().toLowerCase();
        String cleanPassword = require(password, "비밀번호를 입력해 주세요.");

        if (!cleanMemberId.matches("^[a-z0-9]{4,16}$")) {
            throw new IllegalArgumentException("아이디는 영문 소문자/숫자 4~16자로 입력해 주세요.");
        }
        if (cleanPassword.length() < 8 || cleanPassword.length() > 32) {
            throw new IllegalArgumentException("비밀번호는 8~32자로 입력해 주세요.");
        }
        if (!cleanPassword.equals(passwordConfirm)) {
            throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
        }
        if (!cleanEmail.contains("@")) {
            throw new IllegalArgumentException("올바른 이메일을 입력해 주세요.");
        }
        if (!agreeTerms) {
            throw new IllegalArgumentException("이용약관 및 개인정보 수집에 동의해 주세요.");
        }
        if (memberJpaRepository.existsByProviderAndProviderUserId(LOCAL_PROVIDER, cleanMemberId)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (memberJpaRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        MemberEntity member = memberJpaRepository.save(new MemberEntity(
            cleanEmail,
            cleanName,
            cleanName,
            LOCAL_PROVIDER,
            cleanMemberId,
            hashPassword(cleanPassword)
        ));

        return toLoginUser(member);
    }

    @Transactional(readOnly = true)
    public LoginUser login(String memberId, String password) {
        String cleanMemberId = require(memberId, "아이디를 입력해 주세요.").trim();
        String cleanPassword = require(password, "비밀번호를 입력해 주세요.");
        MemberEntity member = memberJpaRepository.findByProviderAndProviderUserId(LOCAL_PROVIDER, cleanMemberId)
            .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호를 확인해 주세요."));

        if (member.getPasswordHash() == null || !verifyPassword(cleanPassword, member.getPasswordHash())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호를 확인해 주세요.");
        }

        return toLoginUser(member);
    }

    private LoginUser toLoginUser(MemberEntity member) {
        return new LoginUser(
            member.getProvider(),
            member.getProviderUserId(),
            member.getEmail(),
            member.getName(),
            member.getNickname()
        );
    }

    private String hashPassword(String password) {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        byte[] hash = pbkdf2(password, salt);
        return "PBKDF2$" + ITERATIONS + "$"
            + Base64.getEncoder().encodeToString(salt) + "$"
            + Base64.getEncoder().encodeToString(hash);
    }

    private boolean verifyPassword(String password, String encoded) {
        String[] parts = encoded.split("\\$");
        if (parts.length != 4 || !"PBKDF2".equals(parts[0])) {
            return false;
        }
        byte[] salt = Base64.getDecoder().decode(parts[2]);
        byte[] expected = Base64.getDecoder().decode(parts[3]);
        byte[] actual = pbkdf2(password, salt);
        return java.security.MessageDigest.isEqual(expected, actual);
    }

    private byte[] pbkdf2(String password, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new IllegalStateException("비밀번호 암호화에 실패했습니다.", ex);
        }
    }

    private String require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return new String(value.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}
