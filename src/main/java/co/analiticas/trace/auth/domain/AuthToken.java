package co.analiticas.trace.auth.domain;

import java.time.Instant;

public record AuthToken(String value, String username, Instant expiresAt) {

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }
}
