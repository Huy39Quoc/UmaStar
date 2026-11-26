package umastar.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import umastar.enums.Role;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * Tao va xac thuc access token (JWT, ky HS256).
 * Token chua: sub = id nguoi dung, role, iss, iat, exp, jti. KHONG chua email hay mat khau.
 * Refresh token KHONG phai JWT: la chuoi ngau nhien, xem SecureTokenGenerator / TokenHasher.
 */
@Component
public class JwtService {

    static final String ISSUER = "umastar";
    private static final String CLAIM_ROLE = "role";
    private static final int MIN_SECRET_BYTES = 32;   // HS256 yeu cau khoa >= 256 bit

    private final SecretKey key;
    private final long accessTtlMs;
    private final Clock clock;

    @Autowired
    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-exp-ms}") long accessTtlMs) {
        this(secret, accessTtlMs, Clock.systemUTC());
    }

    /** Dung cho test: cho phep dieu khien thoi gian. */
    JwtService(String secret, long accessTtlMs, Clock clock) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT_SECRET phai dai it nhat " + MIN_SECRET_BYTES + " ky tu (256 bit)");
        }
        if (accessTtlMs <= 0) {
            throw new IllegalStateException("JWT_ACCESS_EXP_MS phai lon hon 0");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlMs = accessTtlMs;
        this.clock = Objects.requireNonNull(clock);
    }

    public String generateAccessToken(UUID userId, Role role) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(role, "role must not be null");
        Instant now = clock.instant();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(userId.toString())
                .claim(CLAIM_ROLE, role.name())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTtlMs)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Xac thuc chu ky, issuer, han su dung roi tra ve noi dung.
     * @throws AppException TOKEN_EXPIRED neu het han; INVALID_TOKEN cho moi truong hop sai khac
     */
    public AccessTokenClaims parse(String token) {
        if (token == null || token.isBlank()) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(ISSUER)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String subject = claims.getSubject();
            String roleName = claims.get(CLAIM_ROLE, String.class);
            Date issuedAt = claims.getIssuedAt();
            Date expiration = claims.getExpiration();
            if (subject == null || roleName == null || issuedAt == null || expiration == null) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }
            return new AccessTokenClaims(
                    UUID.fromString(subject),
                    Role.valueOf(roleName),
                    issuedAt.toInstant(),
                    expiration.toInstant());
        } catch (ExpiredJwtException e) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            // JwtException: sai chu ky, sai dinh dang, sai issuer...; IllegalArgumentException: sub khong phai UUID, role la
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (AppException e) {
            return false;
        }
    }

    /** Thoi gian song cua access token (giay), tra ve cho FE o truong expiresIn. */
    public long getAccessTokenTtlSeconds() {
        return accessTtlMs / 1000;
    }
}
