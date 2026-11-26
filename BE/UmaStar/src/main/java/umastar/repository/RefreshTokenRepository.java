package umastar.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umastar.entity.RefreshToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @EntityGraph(attributePaths = "user")
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            UPDATE RefreshToken rt
            SET rt.revoked = true, rt.revokedAt = :now, rt.revokedReason = :reason
            WHERE rt.user.id = :userId AND rt.revoked = false
            """)
    int revokeAllActiveByUserId(@Param("userId") UUID userId,
                                @Param("now") Instant now,
                                @Param("reason") String reason);

    @Modifying
    @Query("""
            UPDATE RefreshToken rt
            SET rt.revoked = true, rt.revokedAt = :now, rt.revokedReason = :reason
            WHERE rt.familyId = :familyId AND rt.revoked = false
            """)
    int revokeFamily(@Param("familyId") UUID familyId,
                     @Param("now") Instant now,
                     @Param("reason") String reason);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") Instant cutoff);
}

