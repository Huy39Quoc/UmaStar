package umastar.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    /**
     * Tìm token theo hash và KHÓA DÒNG (SELECT ... FOR UPDATE) cho luồng làm mới: hai request dùng cùng một token
     * phải xếp hàng, request đến sau sẽ thấy token đã bị thu hồi thay vì cùng xoay vòng song song (làm gãy họ token).
     * Không dùng @EntityGraph vì FOR UPDATE kết hợp JOIN dễ lỗi trên PostgreSQL; user được nạp lười trong transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findWithLockByTokenHash(String tokenHash);

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
