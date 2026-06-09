package com.ronnefeldt.model;

import java.io.Serializable;

public record LoginUser(
    String provider,
    String providerId,
    String email,
    String name,
    String nickname
) implements Serializable {

    public String displayName() {
        if (nickname != null && !nickname.isBlank()) {
            return nickname;
        }
        if (name != null && !name.isBlank()) {
            return name;
        }
        if (email != null && !email.isBlank()) {
            return email;
        }
        return "회원";
    }
}
