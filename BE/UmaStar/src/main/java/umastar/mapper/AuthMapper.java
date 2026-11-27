package umastar.mapper;

import org.springframework.stereotype.Component;
import umastar.dto.request.RegisterRequest;
import umastar.dto.response.AuthResponse;
import umastar.dto.response.RegisterResponse;
import umastar.dto.response.UserSummaryResponse;
import umastar.entity.Customer;
import umastar.entity.RefreshToken;
import umastar.entity.User;
import umastar.enums.Role;
import umastar.enums.UserStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Chuyển đổi DTO ↔ Entity cho module xác thực & tài khoản ({@code auth}).
 */
@Component
public class AuthMapper {

    private static final int MAX_USER_AGENT_LENGTH = 500;
    private static final int MAX_IP_LENGTH = 45;

    public User toCustomerUser(RegisterRequest request, String encodedPassword) {
        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPasswordHash(encodedPassword);
        user.setRole(Role.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        return user;
    }

    public Customer toCustomer(RegisterRequest request, User user) {
        Customer customer = new Customer();
        customer.setUser(user);
        customer.setOrganizationName(request.organizationName());
        customer.setCustomerType(request.customerType());
        return customer;
    }

    public RegisterResponse toRegisterResponse(User user, Customer customer) {
        return new RegisterResponse(
                user.getId(),
                customer.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                customer.getOrganizationName(),
                customer.getCustomerType()
        );
    }

    public UserSummaryResponse toUserSummary(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }

    public AuthResponse toAuthResponse(String accessToken, String refreshToken, long expiresInSeconds, User user) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                AuthResponse.BEARER,
                expiresInSeconds,
                toUserSummary(user)
        );
    }

    public RefreshToken toRefreshToken(User user,
                                       String tokenHash,
                                       UUID familyId,
                                       Instant expiresAt,
                                       Instant now,
                                       String userAgent,
                                       String ipAddress) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(tokenHash);
        token.setFamilyId(familyId);
        token.setExpiresAt(expiresAt);
        token.setRevoked(false);
        token.setLastUsedAt(now);
        token.setUserAgent(truncate(userAgent, MAX_USER_AGENT_LENGTH));
        token.setIpAddress(truncate(ipAddress, MAX_IP_LENGTH));
        return token;
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
