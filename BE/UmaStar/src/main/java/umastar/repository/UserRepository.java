package umastar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umastar.entity.User;
import umastar.enums.Role;
import umastar.enums.UserStatus;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /** Tim theo email, khong phan biet hoa thuong (dung cho dang nhap). */
    Optional<User> findByEmailIgnoreCase(String email);

    /** Kiem tra email da duoc dung chua (dung khi dang ky, tao nhan vien). */
    boolean existsByEmailIgnoreCase(String email);

    /** Tim user theo SHA-256 hash cua reset token (dung cho dat lai mat khau). */
    Optional<User> findByResetTokenHash(String resetTokenHash);

    /** Da co user nao mang role nay chua (dung de seed Manager dau tien). */
    boolean existsByRole(Role role);

    /** Dem so user theo role va trang thai (dung de bao ve Manager cuoi cung). */
    long countByRoleAndStatus(Role role, UserStatus status);
}