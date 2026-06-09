package com.ronnefeldt.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("aiven")
public class AivenDbStatusController {

    private final Environment environment;
    private final DataSource dataSource;
    private final String url;
    private final String username;
    private final String password;

    public AivenDbStatusController(
        Environment environment,
        DataSource dataSource,
        @Value("${spring.datasource.url:}") String url,
        @Value("${spring.datasource.username:}") String username,
        @Value("${spring.datasource.password:}") String password
    ) {
        this.environment = environment;
        this.dataSource = dataSource;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @GetMapping("/dev/db-status")
    public String dbStatus() {
        if (password == null || password.isBlank()) {
            return """
                Aiven DB profile is active, but password is not configured.
                Set AIVEN_DB_PASSWORD in your local launch environment.
                activeProfiles=%s
                url=%s
                username=%s
                """.formatted(Arrays.toString(environment.getActiveProfiles()), maskUrl(url), username);
        }

        long startNanos = System.nanoTime();
        long connectionNanos;
        long statementNanos;
        long queryNanos;
        long readNanos;
        try (Connection connection = dataSource.getConnection()) {
            connectionNanos = System.nanoTime();
            try (Statement statement = connection.createStatement()) {
                statementNanos = System.nanoTime();
                try (ResultSet resultSet = statement.executeQuery("SELECT DATABASE(), CURRENT_USER(), VERSION()")) {
                    queryNanos = System.nanoTime();
                    resultSet.next();
                    readNanos = System.nanoTime();

                    long elapsedMillis = millisSince(startNanos);
                    return """
                        Aiven DB connection succeeded.
                        activeProfiles=%s
                        database=%s
                        currentUser=%s
                        mysqlVersion=%s
                        elapsedMs=%d
                        acquireConnectionMs=%d
                        createStatementMs=%d
                        executeQueryMs=%d
                        readResultMs=%d
                        pool=%s
                        """.formatted(
                        Arrays.toString(environment.getActiveProfiles()),
                        resultSet.getString(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        elapsedMillis,
                        nanosToMillis(connectionNanos - startNanos),
                        nanosToMillis(statementNanos - connectionNanos),
                        nanosToMillis(queryNanos - statementNanos),
                        nanosToMillis(readNanos - queryNanos),
                        poolStatus()
                    );
                }
            }
        } catch (Exception exception) {
            return """
                Aiven DB connection failed.
                activeProfiles=%s
                url=%s
                username=%s
                error=%s: %s
                """.formatted(
                Arrays.toString(environment.getActiveProfiles()),
                maskUrl(url),
                username,
                exception.getClass().getSimpleName(),
                exception.getMessage() + rootCauseMessage(exception)
            );
        }
    }

    private long millisSince(long startNanos) {
        return nanosToMillis(System.nanoTime() - startNanos);
    }

    private long nanosToMillis(long nanos) {
        return nanos / 1_000_000;
    }

    private String poolStatus() {
        if (dataSource instanceof HikariDataSource hikariDataSource && hikariDataSource.getHikariPoolMXBean() != null) {
            var pool = hikariDataSource.getHikariPoolMXBean();
            return "active=%d idle=%d total=%d waiting=%d".formatted(
                pool.getActiveConnections(),
                pool.getIdleConnections(),
                pool.getTotalConnections(),
                pool.getThreadsAwaitingConnection()
            );
        }
        return dataSource.getClass().getSimpleName();
    }

    private String maskUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return "";
        }
        return rawUrl.replaceAll("(?i)(password=)[^&]+", "$1****");
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        if (rootCause == throwable) {
            return "";
        }
        return "%nrootCause=%s: %s".formatted(rootCause.getClass().getSimpleName(), rootCause.getMessage());
    }
}
