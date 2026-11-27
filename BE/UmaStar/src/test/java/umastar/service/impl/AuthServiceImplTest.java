package umastar.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import umastar.dto.request.ChangePasswordRequest;
import umastar.dto.request.ForgotPasswordRequest;
import umastar.dto.request.LoginRequest;
import umastar.dto.request.RefreshTokenRequest;
import umastar.dto.request.RegisterRequest;
import umastar.dto.request.ResetPasswordRequest;
import umastar.dto.response.AuthResponse;
import umastar.dto.response.MessageResponse;
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
import umastar.service.MailService;
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
    private static final long RESET_TTL_MS = 1_800_000L;
    private static final long RESET_COOLDOWN_MS = 60_000L;

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CustomerRepository customerRepository = mock(CustomerRepository.class);
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final MailService mailService = mock(MailService.class);
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
            mailService,
            REFRESH_TTL_MS,
            RESET_TTL_MS,
            RESET_COOLDOWN_MS,
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

    @Test
    void refreshRotatesTokenWithinSameFamily() {
        User user = activeUser("club@umastar.local", "MatKhau123");
        UUID familyId = UUID.randomUUID();
        UUID nextId = UUID.randomUUID();
        String oldRaw = "old-raw-refresh-token";
        String oldHash = TokenHasher.sha256Hex(oldRaw);

        RefreshToken current = new RefreshToken();
        current.setId(UUID.randomUUID());
        current.setUser(user);
        current.setTokenHash(oldHash);
        current.setFamilyId(familyId);
        current.setExpiresAt(NOW.plusSeconds(3600));
        current.setRevoked(false);

        when(refreshTokenRepository.findWithLockByTokenHash(oldHash)).thenReturn(Optional.of(current));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> {
            RefreshToken rt = inv.getArgument(0);
            if (rt.getId() == null) {
                rt.setId(nextId);
            }
            return rt;
        });

        AuthResponse response = authService.refresh(new RefreshTokenRequest(oldRaw), "Firefox/131", "10.0.0.2");

        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());
        assertNotEquals(oldRaw, response.refreshToken());
        assertTrue(current.isRevoked());
        assertEquals(NOW, current.getRevokedAt());
        assertEquals(AuthServiceImpl.REVOKED_REASON_ROTATED, current.getRevokedReason());
        assertEquals(nextId, current.getReplacedByTokenId());

        String nextHash = TokenHasher.sha256Hex(response.refreshToken());
        verify(refreshTokenRepository).save(argThat(rt ->
                nextId.equals(rt.getId())
                        && nextHash.equals(rt.getTokenHash())
                        && familyId.equals(rt.getFamilyId())
                        && !rt.isRevoked()
                        && "Firefox/131".equals(rt.getUserAgent())
                        && "10.0.0.2".equals(rt.getIpAddress())
        ));
    }

    @Test
    void refreshRevokedTokenRevokesEntireFamily() {
        User user = activeUser("club@umastar.local", "MatKhau123");
        UUID familyId = UUID.randomUUID();
        String reusedRaw = "reused-revoked-token";
        String reusedHash = TokenHasher.sha256Hex(reusedRaw);

        RefreshToken current = new RefreshToken();
        current.setId(UUID.randomUUID());
        current.setUser(user);
        current.setTokenHash(reusedHash);
        current.setFamilyId(familyId);
        current.setExpiresAt(NOW.plusSeconds(3600));
        current.setRevoked(true);

        when(refreshTokenRepository.findWithLockByTokenHash(reusedHash)).thenReturn(Optional.of(current));
        when(refreshTokenRepository.revokeFamily(familyId, NOW, AuthServiceImpl.REVOKED_REASON_REUSE_DETECTED))
                .thenReturn(1);

        AppException ex = assertThrows(AppException.class,
                () -> authService.refresh(new RefreshTokenRequest(reusedRaw), null, null));
        assertEquals(ErrorCode.REFRESH_TOKEN_REVOKED, ex.getErrorCode());
        verify(refreshTokenRepository).revokeFamily(familyId, NOW, AuthServiceImpl.REVOKED_REASON_REUSE_DETECTED);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refreshRejectsMissingExpiredInactiveAndLockedUser() {
        when(refreshTokenRepository.findWithLockByTokenHash(TokenHasher.sha256Hex("unknown")))
                .thenReturn(Optional.empty());
        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN,
                assertThrows(AppException.class,
                        () -> authService.refresh(new RefreshTokenRequest("unknown"), null, null)).getErrorCode());

        RefreshToken expired = new RefreshToken();
        expired.setUser(activeUser("club@umastar.local", "MatKhau123"));
        expired.setFamilyId(UUID.randomUUID());
        expired.setExpiresAt(NOW);
        expired.setRevoked(false);
        when(refreshTokenRepository.findWithLockByTokenHash(TokenHasher.sha256Hex("expired")))
                .thenReturn(Optional.of(expired));
        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN,
                assertThrows(AppException.class,
                        () -> authService.refresh(new RefreshTokenRequest("expired"), null, null)).getErrorCode());

        User disabledUser = activeUser("disabled@umastar.local", "MatKhau123");
        disabledUser.setStatus(UserStatus.INACTIVE);
        RefreshToken disabledToken = new RefreshToken();
        disabledToken.setUser(disabledUser);
        disabledToken.setFamilyId(UUID.randomUUID());
        disabledToken.setExpiresAt(NOW.plusSeconds(3600));
        disabledToken.setRevoked(false);
        when(refreshTokenRepository.findWithLockByTokenHash(TokenHasher.sha256Hex("disabled")))
                .thenReturn(Optional.of(disabledToken));
        assertEquals(ErrorCode.ACCOUNT_DISABLED,
                assertThrows(AppException.class,
                        () -> authService.refresh(new RefreshTokenRequest("disabled"), null, null)).getErrorCode());

        User lockedUser = activeUser("locked@umastar.local", "MatKhau123");
        lockedUser.setStatus(UserStatus.LOCKED);
        RefreshToken lockedToken = new RefreshToken();
        lockedToken.setUser(lockedUser);
        lockedToken.setFamilyId(UUID.randomUUID());
        lockedToken.setExpiresAt(NOW.plusSeconds(3600));
        lockedToken.setRevoked(false);
        when(refreshTokenRepository.findWithLockByTokenHash(TokenHasher.sha256Hex("locked")))
                .thenReturn(Optional.of(lockedToken));
        assertEquals(ErrorCode.ACCOUNT_LOCKED,
                assertThrows(AppException.class,
                        () -> authService.refresh(new RefreshTokenRequest("locked"), null, null)).getErrorCode());

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void logoutRevokesFamilyIdempotentlyAndLogoutAllRevokesUserTokens() {
        UUID familyId = UUID.randomUUID();
        RefreshToken token = new RefreshToken();
        token.setFamilyId(familyId);
        when(refreshTokenRepository.findWithLockByTokenHash(TokenHasher.sha256Hex("known")))
                .thenReturn(Optional.of(token));
        when(refreshTokenRepository.findWithLockByTokenHash(TokenHasher.sha256Hex("missing")))
                .thenReturn(Optional.empty());

        authService.logout(new RefreshTokenRequest("known"));
        verify(refreshTokenRepository).revokeFamily(familyId, NOW, AuthServiceImpl.REVOKED_REASON_LOGOUT);

        authService.logout(new RefreshTokenRequest("missing"));

        UUID userId = UUID.randomUUID();
        authService.logoutAll(userId);
        verify(refreshTokenRepository).revokeAllActiveByUserId(userId, NOW, AuthServiceImpl.REVOKED_REASON_LOGOUT_ALL);
    }

    @Test
    void changePasswordUpdatesHashSetsTimestampAndRevokesAllTokens() {
        User user = activeUser("club@umastar.local", "OldPass1");
        user.setFailedLoginAttempts(2);
        user.setLockedUntil(NOW.minusSeconds(10));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.changePassword(user.getId(), new ChangePasswordRequest("OldPass1", "NewPass2"));

        assertTrue(passwordEncoder.matches("NewPass2", user.getPasswordHash()));
        assertEquals(NOW, user.getPasswordChangedAt());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
        verify(refreshTokenRepository).revokeAllActiveByUserId(
                user.getId(), NOW, AuthServiceImpl.REVOKED_REASON_PASSWORD_CHANGED);
    }

    @Test
    void changePasswordRejectsWrongOldSamePasswordAndLocksAfterFiveFailures() {
        User user = activeUser("club@umastar.local", "OldPass1");
        user.setFailedLoginAttempts(3);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AppException fourth = assertThrows(AppException.class,
                () -> authService.changePassword(user.getId(), new ChangePasswordRequest("WrongPass1", "NewPass2")));
        assertEquals(ErrorCode.INVALID_OLD_PASSWORD, fourth.getErrorCode());
        assertEquals(4, user.getFailedLoginAttempts());

        AppException fifth = assertThrows(AppException.class,
                () -> authService.changePassword(user.getId(), new ChangePasswordRequest("WrongPass1", "NewPass2")));
        assertEquals(ErrorCode.ACCOUNT_LOCKED, fifth.getErrorCode());
        assertEquals(5, user.getFailedLoginAttempts());
        assertEquals(NOW.plus(AuthServiceImpl.LOCK_DURATION), user.getLockedUntil());

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        AppException same = assertThrows(AppException.class,
                () -> authService.changePassword(user.getId(), new ChangePasswordRequest("OldPass1", "OldPass1")));
        assertEquals(ErrorCode.NEW_PASSWORD_SAME_AS_OLD, same.getErrorCode());
        verify(refreshTokenRepository, never()).revokeAllActiveByUserId(any(), any(), any());
    }

    @Test
    void forgotPasswordStoresTokenHashEnforcesCooldownAndHandlesUnknownEmailUniformly() {
        User user = activeUser("club@umastar.local", "OldPass1");
        when(userRepository.findByEmailIgnoreCase("club@umastar.local")).thenReturn(Optional.of(user));
        when(userRepository.findByEmailIgnoreCase("unknown@umastar.local")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        MessageResponse ack = authService.forgotPassword(new ForgotPasswordRequest("  Club@UmaStar.Local "));
        assertNotNull(ack.message());

        ArgumentCaptor<String> rawTokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailService).sendPasswordResetEmail(eq("club@umastar.local"), eq("Nguyễn Văn An"),
                rawTokenCaptor.capture(), eq(30L));
        assertEquals(TokenHasher.sha256Hex(rawTokenCaptor.getValue()), user.getResetTokenHash());
        assertEquals(NOW.plusMillis(RESET_TTL_MS), user.getResetTokenExpiresAt());
        assertEquals(NOW, user.getResetRequestedAt());

        AppException rateLimited = assertThrows(AppException.class,
                () -> authService.forgotPassword(new ForgotPasswordRequest("club@umastar.local")));
        assertEquals(ErrorCode.TOO_MANY_REQUESTS, rateLimited.getErrorCode());

        MessageResponse unknownAck = authService.forgotPassword(new ForgotPasswordRequest("unknown@umastar.local"));
        assertEquals(ack.message(), unknownAck.message());
        AppException unknownRateLimited = assertThrows(AppException.class,
                () -> authService.forgotPassword(new ForgotPasswordRequest("unknown@umastar.local")));
        assertEquals(ErrorCode.TOO_MANY_REQUESTS, unknownRateLimited.getErrorCode());
        verifyNoMoreInteractions(mailService);
    }

    @Test
    void resetPasswordClearsResetFieldsUpdatesPasswordAndRevokesRefreshTokens() {
        User user = activeUser("club@umastar.local", "OldPass1");
        user.setFailedLoginAttempts(4);
        user.setLockedUntil(NOW.plusSeconds(120));
        user.setResetTokenHash(TokenHasher.sha256Hex("valid-reset-token"));
        user.setResetTokenExpiresAt(NOW.plusSeconds(600));
        user.setResetRequestedAt(NOW.minusSeconds(60));
        when(userRepository.findByResetTokenHash(TokenHasher.sha256Hex("valid-reset-token")))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.resetPassword(new ResetPasswordRequest("  valid-reset-token ", "NewPass2"));

        assertTrue(passwordEncoder.matches("NewPass2", user.getPasswordHash()));
        assertEquals(NOW, user.getPasswordChangedAt());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
        assertNull(user.getResetTokenHash());
        assertNull(user.getResetTokenExpiresAt());
        assertNull(user.getResetRequestedAt());
        verify(refreshTokenRepository).revokeAllActiveByUserId(
                user.getId(), NOW, AuthServiceImpl.REVOKED_REASON_PASSWORD_RESET);
    }

    @Test
    void resetPasswordRejectsExpiredInvalidAndSamePassword() {
        User expiredUser = activeUser("expired@umastar.local", "OldPass1");
        expiredUser.setResetTokenHash(TokenHasher.sha256Hex("expired-token"));
        expiredUser.setResetTokenExpiresAt(NOW);
        expiredUser.setResetRequestedAt(NOW.minusSeconds(1800));
        when(userRepository.findByResetTokenHash(TokenHasher.sha256Hex("expired-token")))
                .thenReturn(Optional.of(expiredUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AppException expiredEx = assertThrows(AppException.class,
                () -> authService.resetPassword(new ResetPasswordRequest("expired-token", "NewPass2")));
        assertEquals(ErrorCode.INVALID_RESET_TOKEN, expiredEx.getErrorCode());
        assertNull(expiredUser.getResetTokenHash());
        assertNull(expiredUser.getResetTokenExpiresAt());

        when(userRepository.findByResetTokenHash(TokenHasher.sha256Hex("missing-token")))
                .thenReturn(Optional.empty());
        assertEquals(ErrorCode.INVALID_RESET_TOKEN,
                assertThrows(AppException.class,
                        () -> authService.resetPassword(new ResetPasswordRequest("missing-token", "NewPass2")))
                        .getErrorCode());

        User sameUser = activeUser("same@umastar.local", "OldPass1");
        sameUser.setResetTokenHash(TokenHasher.sha256Hex("same-token"));
        sameUser.setResetTokenExpiresAt(NOW.plusSeconds(600));
        when(userRepository.findByResetTokenHash(TokenHasher.sha256Hex("same-token")))
                .thenReturn(Optional.of(sameUser));
        assertEquals(ErrorCode.NEW_PASSWORD_SAME_AS_OLD,
                assertThrows(AppException.class,
                        () -> authService.resetPassword(new ResetPasswordRequest("same-token", "OldPass1")))
                        .getErrorCode());
        verify(refreshTokenRepository, never()).revokeAllActiveByUserId(any(), any(), any());
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
