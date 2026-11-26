package umastar.security;

import umastar.enums.Role;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Noi dung da xac thuc cua access token. */
public record AccessTokenClaims(UUID userId, Role role, Instant issuedAt, Instant expiresAt) {

    public boolean issuedBefore(Instant changedAt) {
        return changedAt != null && issuedAt.isBefore(changedAt.truncatedTo(ChronoUnit.SECONDS));
    }
}