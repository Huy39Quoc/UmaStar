package umastar.security;

import umastar.enums.Role;

import java.time.Instant;
import java.util.UUID;

/** Noi dung da xac thuc cua access token. */
public record AccessTokenClaims(UUID userId, Role role, Instant issuedAt, Instant expiresAt) {
}
