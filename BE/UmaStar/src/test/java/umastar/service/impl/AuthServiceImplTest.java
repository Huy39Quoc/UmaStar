package umastar.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import umastar.dto.request.LoginRequest;
import umastar.dto.request.RegisterRequest;
import umastar.dto.response.AuthResponse;
import umastar.dto.response.RegisterResponse;
import umastar.entity.Customer;
import umastar.entity.RefreshToken;
import umastar.entity.User;
import umastar.enums.CustomerType;
import umastar.enums.Role;
import umastar.enums.UserStatus;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;
import umastar.mapper.AuthMapper;
import umastar.repository.CustomerRepository;
import umastar.repository.RefreshTokenRepository;
import umastar.repository.UserRepository;
import umastar.security.JwtService;
import umastar.util.TokenHasher;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    private static final String TEST_SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef";
    private static final Instant NOW = Instant.parse("2026-11-27T07:00:00Z");
    private static final long REFRESH_TTL_MS = 604_800_000L;

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CustomerRepository customerRepository = mock(CustomerRepository.class);
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtService jwtService = new JwtService(TEST_SECRET, 900_000L);
    private final AuthMapper authMapper = new AuthMapper();
    private final Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final AuthServiceImpl authService = new AuthServiceImpl(
            userRepository,
            customerRepository,
            refreshTokenRepository,
            passwordEncoder,
            jwtService,
            authMapper,
            REFRESH_TTL_MS,
            fixedClock
    );

    @Test
    void registerCreatesCustomerUserAndProfile() {
        UUID userId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        when(userRepository.existsByEmailIgnoreCase("club@umastar.local")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(userId);
            return u;
        });
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(customerId);
            return c;
        });

        RegisterRequest request = new RegisterRequest(
                "Nguyễn Văn An",
                "  Club@UmaStar.Local ",
                "MatKhau123",
                "+84901234567",
                "CLB Ánh Sao",
                CustomerType.CLUB
        );

        RegisterResponse response = authService.register(request);

        assertEquals(userId, response.userId());
        assertEquals(customerId, response.customerId());
        assertEquals("Nguyễn Văn An", response.fullName());
        assertEquals("club@umastar.local", response.email());
        assertEquals(Role.CUSTOMER, response.role());
        assertEquals("CLB Ánh Sao", response.organizationName());
        assertEquals(CustomerType.CLUB, response.customerType());

        verify(userRepository).save(argThat(u ->
                u.getRole() == Role.CUSTOMER
                        && u.getStatus() == UserStatus.ACTIVE
                        && !u.isEmailVerified()
                        && "+84901234567".equals(u.getPhone())
                        && passwordEncoder.matches("MatKhau123", u.getPasswordHash())
        ));
        verify(customerRepository).save(argThat(c ->
                c.getUser() != null
                        && userId.equals(c.getUser().getId())
                        && "CLB Ánh Sao".equals(c.getOrganizationName())
                        && c.getCustomerType() == CustomerType.CLUB
        ));
    }

    @Test
    void registerThrowsConflictWhenEmailExists() {
        when(userRepository.existsByEmailIgnoreCase("used@umastar.local")).thenReturn(true);

        RegisterRequest request = new RegisterRequest(
                "Nguyễn Văn An",
                "USED@umastar.local",
                "MatKhau123",
                null,
                "Cá nhân",
                CustomerType.INDIVIDUAL
        );

        AppException ex = assertThrows(AppException.class, () -> authService.register(request));
        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, ex.getErrorCode());
        verify(userRepository, never()).save(any());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void loginSuccessResetsLockoutAndStoresHashedRefreshToken() {
        User user = activeUser("club@umastar.local", "MatKhau123");
        user.setFailedLoginAttempts(3);
        user.setLockedUntil(NOW.minusSeconds(60));
        when(userRepository.findByEmailIgnoreCase("club@umastar.local")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.login(
                new LoginRequest("  Club@UmaStar.Local ", "MatKhau123"),
                "Chrome/130",
                "127.0.0.1"
        );

        assertEquals(AuthResponse.BEARER, response.tokenType());
        assertEquals(900L, response.expiresIn());
        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());
        assertEquals(user.getId(), response.user().id());
        assertEquals(Role.CUSTOMER, response.user().role());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
        assertEquals(NOW, user.getLastLoginAt());

        String expectedHash = TokenHasher.sha256Hex(response.refreshToken());
        verify(refreshTokenRepository).save(argThat(rt ->
                rt.getUser() == user
                        && expectedHash.equals(rt.getTokenHash())
                        && !rt.isRevoked()
                        && NOW.plusMillis(REFRESH_TTL_MS).equals(rt.getExpiresAt())
                        && "Chrome/130".equals(rt.getUserAgent())
                        && "127.0.0.1".equals(rt.getIpAddress())
        ));
    }

    @Test
    void loginWrongPasswordFiveTimesLocksAccountFor15Minutes() {
        User user = activeUser("club@umastar.local", "MatKhau123");
        user.setFailedLoginAttempts(3);
        when(userRepository.findByEmailIgnoreCase("club@umastar.local")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        LoginRequest wrongRequest = new LoginRequest("club@umastar.local", "SaiMatKhau1");

        AppException fourthFail = assertThrows(AppException.class,
                () -> authService.login(wrongRequest, null, null));
        assertEquals(ErrorCode.INVALID_CREDENTIALS, fourthFail.getErrorCode());
        assertEquals(4, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());

        AppException fifthFail = assertThrows(AppException.class,
                () -> authService.login(wrongRequest, null, null));
        assertEquals(ErrorCode.ACCOUNT_LOCKED, fifthFail.getErrorCode());
        assertEquals(5, user.getFailedLoginAttempts());
        assertEquals(NOW.plus(AuthServiceImpl.LOCK_DURATION), user.getLockedUntil());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void loginRejectsUnknownEmailLockedAndDisabledAccounts() {
        when(userRepository.findByEmailIgnoreCase("none@umastar.local")).thenReturn(Optional.empty());
        AppException unknown = assertThrows(AppException.class,
                () -> authService.login(new LoginRequest("none@umastar.local", "MatKhau123"), null, null));
        assertEquals(ErrorCode.INVALID_CREDENTIALS, unknown.getErrorCode());

        User locked = activeUser("locked@umastar.local", "MatKhau123");
        locked.setLockedUntil(NOW.plusSeconds(300));
        when(userRepository.findByEmailIgnoreCase("locked@umastar.local")).thenReturn(Optional.of(locked));
        AppException lockedEx = assertThrows(AppException.class,
                () -> authService.login(new LoginRequest("locked@umastar.local", "MatKhau123"), null, null));
        assertEquals(ErrorCode.ACCOUNT_LOCKED, lockedEx.getErrorCode());

        User disabled = activeUser("disabled@umastar.local", "MatKhau123");
        disabled.setStatus(UserStatus.INACTIVE);
        when(userRepository.findByEmailIgnoreCase("disabled@umastar.local")).thenReturn(Optional.of(disabled));

        AppException disabledWrongPw = assertThrows(AppException.class,
                () -> authService.login(new LoginRequest("disabled@umastar.local", "Sai12345"), null, null));
        assertEquals(ErrorCode.INVALID_CREDENTIALS, disabledWrongPw.getErrorCode());

        AppException disabledRightPw = assertThrows(AppException.class,
                () -> authService.login(new LoginRequest("disabled@umastar.local", "MatKhau123"), null, null));
        assertEquals(ErrorCode.ACCOUNT_DISABLED, disabledRightPw.getErrorCode());
    }

    private User activeUser(String email, String rawPassword) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName("Nguyễn Văn An");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(Role.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}
