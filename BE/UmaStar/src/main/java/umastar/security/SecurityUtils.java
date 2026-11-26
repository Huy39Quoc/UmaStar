package umastar.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import umastar.enums.Role;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;

import java.util.UUID;

/**
 * Helper lay thong tin nguoi dung dang dang nhap tu SecurityContext.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal;
    }

    public static UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public static Role getCurrentRole() {
        return getCurrentUser().getRole();
    }
}

