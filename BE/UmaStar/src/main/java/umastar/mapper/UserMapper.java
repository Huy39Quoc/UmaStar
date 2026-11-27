package umastar.mapper;

import org.springframework.stereotype.Component;
import umastar.dto.request.CreateUserRequest;
import umastar.dto.response.UserResponse;
import umastar.entity.User;
import umastar.enums.UserStatus;

/**
 * Chuyển đổi DTO ↔ Entity cho hồ sơ cá nhân ({@code /profile}) và quản lý người dùng ({@code /users}).
 */
@Component
public class UserMapper {

    public User toStaffUser(CreateUserRequest request, String encodedPassword) {
        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPasswordHash(encodedPassword);
        user.setRole(request.role());
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        return user;
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.isEmailVerified(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }
}

