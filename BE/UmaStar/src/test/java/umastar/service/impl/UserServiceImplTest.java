package umastar.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import umastar.dto.request.CreateUserRequest;
import umastar.dto.request.UpdateProfileRequest;
import umastar.dto.request.UpdateUserStatusRequest;
import umastar.dto.response.UserResponse;
import umastar.entity.User;
import umastar.enums.Role;
import umastar.enums.UserStatus;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;
import umastar.mapper.UserMapper;
import umastar.repository.RefreshTokenRepository;
import umastar.repository.UserRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-11-27T08:00:00Z");

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserMapper userMapper = new UserMapper();
    private final UserServiceImpl userService = new UserServiceImpl(
            userRepository,
            refreshTokenRepository,
            passwordEncoder,
            userMapper,
            Clock.fixed(NOW, ZoneOffset.UTC)
    );

    @Test
    void getAndUpdateMyProfileUpdatesNameAndPhoneOnly() {
        User user = user(UUID.randomUUID(), "driver@umastar.local", Role.DRIVER, UserStatus.ACTIVE);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse initial = userService.getProfile(user.getId());
        assertEquals("driver@umastar.local", initial.email());

        UserResponse updated = userService.updateProfile(
                user.getId(), new UpdateProfileRequest("  Trần Văn Tài ", "+84988777666"));
        assertEquals("Trần Văn Tài", updated.fullName());
        assertEquals("+84988777666", updated.phone());
        assertEquals(Role.DRIVER, updated.role());
        assertEquals("driver@umastar.local", updated.email());
    }

    @Test
    void createUserValidatesStaffRoleAndDuplicateEmail() {
        assertEquals(ErrorCode.INVALID_STAFF_ROLE,
                assertThrows(AppException.class, () -> userService.createUser(new CreateUserRequest(
                        "Khách Hàng", "c@umastar.local", "Password1", null, Role.CUSTOMER)))
                        .getErrorCode());

        when(userRepository.existsByEmailIgnoreCase("used@umastar.local")).thenReturn(true);
        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS,
                assertThrows(AppException.class, () -> userService.createUser(new CreateUserRequest(
                        "Nhân Viên", "USED@umastar.local", "Password1", null, Role.ESCORT)))
                        .getErrorCode());

        when(userRepository.existsByEmailIgnoreCase("escort@umastar.local")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        UserResponse created = userService.createUser(new CreateUserRequest(
                "  Lê Đi Kèm ", "Escort@UmaStar.Local", "Password1", "0909000111", Role.ESCORT));
        assertEquals(Role.ESCORT, created.role());
        assertEquals(UserStatus.ACTIVE, created.status());
        assertTrue(created.emailVerified());
        assertEquals("escort@umastar.local", created.email());
    }

    @Test
    void updateUserStatusEnforcesSelfAndLastManagerGuardsAndRevokesTokens() {
        UUID managerId = UUID.randomUUID();
        User manager = user(managerId, "mgr@umastar.local", Role.LOGISTICS_MANAGER, UserStatus.ACTIVE);
        when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));

        assertEquals(ErrorCode.CANNOT_DEACTIVATE_SELF,
                assertThrows(AppException.class, () -> userService.updateUserStatus(
                        managerId, managerId, new UpdateUserStatusRequest(UserStatus.INACTIVE)))
                        .getErrorCode());

        UUID otherManagerId = UUID.randomUUID();
        User otherManager = user(otherManagerId, "mgr2@umastar.local", Role.LOGISTICS_MANAGER, UserStatus.ACTIVE);
        when(userRepository.findById(otherManagerId)).thenReturn(Optional.of(otherManager));
        when(userRepository.countByRoleAndStatus(Role.LOGISTICS_MANAGER, UserStatus.ACTIVE)).thenReturn(1L);

        assertEquals(ErrorCode.LAST_MANAGER_PROTECTED,
                assertThrows(AppException.class, () -> userService.updateUserStatus(
                        managerId, otherManagerId, new UpdateUserStatusRequest(UserStatus.INACTIVE)))
                        .getErrorCode());

        UUID driverId = UUID.randomUUID();
        User driver = user(driverId, "drv@umastar.local", Role.DRIVER, UserStatus.ACTIVE);
        when(userRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse deactivated = userService.updateUserStatus(
                managerId, driverId, new UpdateUserStatusRequest(UserStatus.INACTIVE));
        assertEquals(UserStatus.INACTIVE, deactivated.status());
        verify(refreshTokenRepository).revokeAllActiveByUserId(
                driverId, NOW, UserServiceImpl.REVOKED_REASON_STATUS_CHANGED);

        driver.setFailedLoginAttempts(5);
        driver.setLockedUntil(NOW.plusSeconds(600));
        UserResponse reactivated = userService.updateUserStatus(
                managerId, driverId, new UpdateUserStatusRequest(UserStatus.ACTIVE));
        assertEquals(UserStatus.ACTIVE, reactivated.status());
        assertEquals(0, driver.getFailedLoginAttempts());
        assertNull(driver.getLockedUntil());
    }

    private User user(UUID id, String email, Role role, UserStatus status) {
        User u = new User();
        u.setId(id);
        u.setFullName("Người Dùng Mẫu");
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode("Password1"));
        u.setRole(role);
        u.setStatus(status);
        return u;
    }
}

