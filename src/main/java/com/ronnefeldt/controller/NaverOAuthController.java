package com.ronnefeldt.controller;

import java.net.URI;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ronnefeldt.model.LoginUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class NaverOAuthController {

    private static final String STATE_SESSION_KEY = "NAVER_OAUTH_STATE";

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final RestClient naverAuthClient;
    private final RestClient naverProfileClient;

    public NaverOAuthController(
        @Value("${naver.oauth.client-id:}") String clientId,
        @Value("${naver.oauth.client-secret:}") String clientSecret,
        @Value("${naver.oauth.redirect-uri:http://localhost:8080/oauth/naver/callback}") String redirectUri
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.naverAuthClient = RestClient.create("https://nid.naver.com");
        this.naverProfileClient = RestClient.create("https://openapi.naver.com");
    }

    @GetMapping("/oauth/naver/login")
    public String login(HttpSession session) {
        if (isBlank(clientId) || isBlank(clientSecret)) {
            return "redirect:/oauth/naver/not-configured";
        }

        String state = UUID.randomUUID().toString();
        session.setAttribute(STATE_SESSION_KEY, state);

        URI authorizeUri = UriComponentsBuilder
            .fromUriString("https://nid.naver.com/oauth2.0/authorize")
            .queryParam("response_type", "code")
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("state", state)
            .build()
            .toUri();

        return "redirect:" + authorizeUri;
    }

    @GetMapping("/oauth/naver/callback")
    public ResponseEntity<String> callback(
        @RequestParam String code,
        @RequestParam String state,
        HttpSession session
    ) {
        Object savedState = session.getAttribute(STATE_SESSION_KEY);
        session.removeAttribute(STATE_SESSION_KEY);

        if (!state.equals(savedState)) {
            return plainText("Invalid Naver OAuth state.", 400);
        }

        NaverTokenResponse token = naverAuthClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/oauth2.0/token")
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("code", code)
                .queryParam("state", state)
                .build())
            .retrieve()
            .body(NaverTokenResponse.class);

        if (token == null || isBlank(token.accessToken())) {
            return plainText("Naver OAuth token was not returned.", 502);
        }

        NaverProfileResponse profile = naverProfileClient.get()
            .uri("/v1/nid/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
            .retrieve()
            .body(NaverProfileResponse.class);

        if (profile == null || profile.response() == null) {
            return plainText("Naver profile was not returned.", 502);
        }

        NaverProfile user = profile.response();
        session.setAttribute("loginUser", new LoginUser(
            "naver",
            valueOrBlank(user.id()),
            valueOrBlank(user.email()),
            valueOrBlank(user.name()),
            valueOrBlank(user.nickname())
        ));

        return ResponseEntity.status(302)
            .header(HttpHeaders.LOCATION, "/")
            .build();
    }

    @GetMapping("/oauth/naver/not-configured")
    public ResponseEntity<String> notConfigured() {
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body("""
                Naver OAuth is not configured.

                Set NAVER_CLIENT_ID and NAVER_CLIENT_SECRET in your local launch environment.
                NAVER_REDIRECT_URI is optional. Default: http://localhost:8080/oauth/naver/callback
                """);
    }

    private static ResponseEntity<String> plainText(String body, int status) {
        return ResponseEntity.status(status)
            .contentType(MediaType.TEXT_PLAIN)
            .body(body);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String valueOrBlank(String value) {
        return value == null ? "" : value;
    }

    public record NaverTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") Long expiresIn
    ) {
    }

    public record NaverProfileResponse(
        String resultcode,
        String message,
        NaverProfile response
    ) {
    }

    public record NaverProfile(
        String id,
        String nickname,
        String name,
        String email,
        String mobile
    ) {
    }
}
