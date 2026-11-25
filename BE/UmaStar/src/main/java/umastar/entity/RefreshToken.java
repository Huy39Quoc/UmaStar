package umastar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import umastar.enums.*;

/**
 * Refresh token (rotation). Chỉ lưu HASH (SHA-256) của token, không lưu token thô.
 * Mỗi lần refresh: revoke token cũ, set replacedByTokenId, phát hành token mới cùng familyId.
 * Nếu token đã revoke bị dùng lại -> revoke toàn bộ family (phát hiện token bị đánh cắp).
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_user", columnList = "user_id"),
        @Index(name = "idx_refresh_family", columnList = "family_id"),
        @Index(name = "idx_refresh_expires", columnList = "expires_at")
})
@Getter @Setter @NoArgsConstructor
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 128)
    private String tokenHash;

    @Column(name = "family_id", nullable = false, columnDefinition = "uuid")
    private UUID familyId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoked_reason", length = 100)
    private String revokedReason;

    @Column(name = "replaced_by_token_id", columnDefinition = "uuid")
    private UUID replacedByTokenId;

    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Transient
    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    @Transient
    public boolean isActive() {
        return !revoked && !isExpired();
    }
}
