package umastar.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import umastar.entity.User;
import umastar.enums.Role;
import umastar.enums.UserStatus;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Nguoi dung dang dang nhap, dung trong SecurityContext. Chi giu cac truong can thiet
 * (khong giu entity User de tranh lazy loading va lo du lieu).
 * Quyen duoc dat dang ROLE_<TEN_ROLE> nen dung duoc @PreAuthorize("hasRole('LOGISTICS_MANAGER')").
 */
public final class UserPrincipal implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final UserStatus status;
    private final Instant lockedUntil;
    private final Instant passwordChangedAt;

    public UserPrincipal(UUID id, String email, String passwordHash, Role role,
                         UserStatus status, Instant lockedUntil, Instant passwordChangedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
        this.lockedUntil = lockedUntil;
        this.passwordChangedAt = passwordChangedAt;
    }

    public static UserPrincipal from(User user) {
        return new UserPrincipal(user.getId(), user.getEmail(), user.getPasswordHash(),
                user.getRole(), user.getStatus(), user.getLockedUntil(), user.getPasswordChangedAt());
    }

    public UUID getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Instant getPasswordChangedAt() {
        return passwordChangedAt;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    /** Dinh danh dang nhap la email. */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** Bi khoa khi status = LOCKED, hoac dang trong thoi gian khoa tam do dang nhap sai nhieu lan. */
    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED
                && (lockedUntil == null || !lockedUntil.isAfter(Instant.now()));
    }

    /** Bi vo hieu hoa khi status = INACTIVE. */
    @Override
    public boolean isEnabled() {
        return status != UserStatus.INACTIVE;
    }

    /** Khong in password hash ra log. */
    @Override
    public String toString() {
        return "UserPrincipal{id=" + id + ", email=" + email + ", role=" + role + ", status=" + status + "}";
    }
}
