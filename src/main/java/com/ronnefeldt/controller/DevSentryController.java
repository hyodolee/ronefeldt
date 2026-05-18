package com.ronnefeldt.controller;

import java.util.Arrays;

import io.sentry.Sentry;
import io.sentry.protocol.SentryId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile({"local", "dev"})
public class DevSentryController {

    private final Environment environment;
    private final String sentryDsn;

    public DevSentryController(Environment environment, @Value("${sentry.dsn:}") String sentryDsn) {
        this.environment = environment;
        this.sentryDsn = sentryDsn;
    }

    @GetMapping("/dev/sentry-status")
    public String sentryStatus() {
        return """
            Sentry local test endpoint is active.
            activeProfiles=%s
            sentryDsnConfigured=%s
            """.formatted(
            Arrays.toString(environment.getActiveProfiles()),
            sentryDsn != null && !sentryDsn.isBlank()
        );
    }

    @GetMapping("/dev/sentry-capture")
    public String sentryCapture() {
        SentryId eventId = Sentry.captureException(new IllegalStateException("Sentry local manual capture test"));
        return "Sentry manual capture requested. eventId=" + eventId;
    }

    @GetMapping("/dev/sentry-test")
    public String sentryTest() {
        throw new IllegalStateException("Sentry local test error");
    }
}
