package umastar.security;

import org.junit.jupiter.api.Test;
import umastar.enums.Role;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String TEST_SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef";

    @Test
    void generateAndParseValidAccessToken() {
        Instant fixedNow = Instant.parse("2026-11-26T00:00:00Z");
        JwtService jwtService = new JwtService(TEST_SECRET, 900_000L, Clock.fixed(fixedNow, ZoneOffset.UTC));
        UUID userId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, Role.LOGISTICS_MANAGER);
        AccessTokenClaims claims = jwtService.parse(token);

        assertEquals(userId, claims.userId());
        assertEquals(Role.LOGISTICS_MANAGER, claims.role());
        assertEquals(fixedNow, claims.issuedAt());
        assertEquals(fixedNow.plusMillis(900_000L), claims.expiresAt());
        assertEquals(900L, jwtService.getAccessTokenTtlSeconds());
        assertTrue(jwtService.isValid(token));
    }

    @Test
    void expiredTokenThrowsTokenExpired() {
        Instant issuedAt = Instant.parse("2026-11-26T00:00:00Z");
        JwtService issuer = new JwtService(TEST_SECRET, 60_000L, Clock.fixed(issuedAt, ZoneOffset.UTC));
        String token = issuer.generateAccessToken(UUID.randomUUID(), Role.CUSTOMER);

        JwtService verifierLater = new JwtService(TEST_SECRET, 60_000L,
                Clock.fixed(issuedAt.plusSeconds(120), ZoneOffset.UTC));

        AppException ex = assertThrows(AppException.class, () -> verifierLater.parse(token));
        assertEquals(ErrorCode.TOKEN_EXPIRED, ex.getErrorCode());
        assertFalse(verifierLater.isValid(token));
    }

    @Test
    void tokenSignedWithOtherKeyThrowsInvalidToken() {
        JwtService issuer = new JwtService(TEST_SECRET, 60_000L, Clock.systemUTC());
        String token = issuer.generateAccessToken(UUID.randomUUID(), Role.DRIVER);

        JwtService otherVerifier = new JwtService("zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz", 60_000L,
                Clock.systemUTC());

        AppException ex = assertThrows(AppException.class, () -> otherVerifier.parse(token));
        assertEquals(ErrorCode.INVALID_TOKEN, ex.getErrorCode());
    }
}

