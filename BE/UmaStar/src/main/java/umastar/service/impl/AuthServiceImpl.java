package umastar.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import umastar.enums.UserStatus;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;
import umastar.mapper.AuthMapper;
import umastar.repository.CustomerRepository;
import umastar.repository.RefreshTokenRepository;
import umastar.repository.UserRepository;
import umastar.security.JwtService;
import umastar.service.AuthService;
import umastar.service.MailService;
import umastar.util.SecureTokenGenerator;
import umastar.util.TokenHasher;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    /** Lý do thu hồi: token cũ bị thay bằng token mới khi làm mới bình thường. */
    static final String REVOKED_REASON_ROTATED = "ROTATED";
    /** Lý do thu hồi: token đã thu hồi bị dùng lại -> nghi bị đánh cắp, thu hồi cả họ token. */
    static final String REVOKED_REASON_REUSE_DETECTED = "REUSE_DETECTED";
    /** Lý do thu hồi: người dùng đăng xuất khỏi một thiết bị. */
    static final String REVOKED_REASON_LOGOUT = "LOGOUT";
    /** Lý do thu hồi: người dùng đăng xuất khỏi tất cả thiết bị. */
    static final String REVOKED_REASON_LOGOUT_ALL = "LOGOUT_ALL";
    /** Lý do thu hồi: người dùng vừa đổi mật khẩu nên mọi phiên cũ phải đăng nhập lại. */
    static final String REVOKED_REASON_PASSWORD_CHANGED = "PASSWORD_CHANGED";
    /** Lý do thu hồi: người dùng vừa đặt lại mật khẩu qua email. */
    static final String REVOKED_REASON_PASSWORD_RESET = "PASSWORD_RESET";

    static final int MAX_FAILED_ATTEMPTS = 5;
    static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private static final int MAX_COOLDOWN_ENTRIES = 10_000;
    private static final String FORGOT_PASSWORD_ACK =
            "Nếu email tồn tại trong hệ thống, hướng dẫn đặt lại mật khẩu đã được gửi";

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final MailService mailService;
    private final long refreshTtlMs;
    private final long resetTokenTtlMs;
    private final long resetCooldownMs;
    private final Clock clock;
    private final String dummyHash;
    private final ConcurrentMap<String, Instant> resetCooldownByEmail = new ConcurrentHashMap<>();

    @Autowired
    public AuthServiceImpl(UserRepository userRepository,
                           CustomerRepository customerRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           AuthMapper authMapper,
                           MailService mailService,
                           @Value("${app.jwt.refresh-exp-ms}") long refreshTtlMs,
                           @Value("${app.auth.reset-token-exp-ms:1800000}") long resetTokenTtlMs,
                           @Value("${app.auth.reset-cooldown-ms:60000}") long resetCooldownMs) {
        this(userRepository, customerRepository, refreshTokenRepository, passwordEncoder,
                jwtService, authMapper, mailService, refreshTtlMs, resetTokenTtlMs, resetCooldownMs, Clock.systemUTC());
    }

    AuthServiceImpl(UserRepository userRepository,
                    CustomerRepository customerRepository,
                    RefreshTokenRepository refreshTokenRepository,
                    PasswordEncoder passwordEncoder,
                    JwtService jwtService,
                    AuthMapper authMapper,
                    MailService mailService,
                    long refreshTtlMs,
                    long resetTokenTtlMs,
                    long resetCooldownMs,
                    Clock clock) {
        if (refreshTtlMs <= 0) {
            throw new IllegalStateException("JWT_REFRESH_EXP_MS phai lon hon 0");
        }
        if (resetTokenTtlMs <= 0) {
            throw new IllegalStateException("RESET_TOKEN_EXP_MS phai lon hon 0");
        }
        if (resetCooldownMs < 0) {
            throw new IllegalStateException("RESET_COOLDOWN_MS khong duoc am");
        }
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authMapper = authMapper;
        this.mailService = mailService;
        this.refreshTtlMs = refreshTtlMs;
        this.resetTokenTtlMs = resetTokenTtlMs;
        this.resetCooldownMs = resetCooldownMs;
        this.clock = Objects.requireNonNull(clock);
        // Hash gia dung khi khong tim thay email nham giu thoi gian phan hoi dong deu, tranh do email.
        this.dummyHash = passwordEncoder.encode("UmaStar@DummyTimingCheck1");
    }

    /**
     * Quy trình: kiểm tra email chưa dùng -> mã hóa mật khẩu (BCrypt) -> lưu User(CUSTOMER) -> lưu Customer liên kết 1-1.
     * Hai lần đăng ký trùng email cùng lúc: bước kiểm tra không chặn được, nhưng ràng buộc {@code uk_users_email}
     * của DB sẽ chặn và {@code GlobalExceptionHandler} đổi thành EMAIL_ALREADY_EXISTS.
     */
    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User savedUser = userRepository.save(
                authMapper.toCustomerUser(request, passwordEncoder.encode(request.password())));
        Customer savedCustomer = customerRepository.save(authMapper.toCustomer(request, savedUser));

        return authMapper.toRegisterResponse(savedUser, savedCustomer);
    }

    /**
     * Quy trình đăng nhập:
     * 1. Tìm user theo email (không tìm thấy vẫn chạy BCrypt giả rồi báo INVALID_CREDENTIALS).
     * 2. Chặn tài khoản đang bị khóa (UserStatus.LOCKED hoặc lockedUntil còn hiệu lực).
     * 3. Nếu đã hết thời gian khóa tạm, reset bộ đếm lỗi; kiểm tra mật khẩu.
     * 4. Sai mật khẩu -> tăng failedLoginAttempts, đạt 5 lần thì khóa 15 phút (noRollbackFor giữ được bộ đếm).
     * 5. Đúng mật khẩu -> chặn nếu INACTIVE, ngược lại reset bộ đếm, ghi lastLoginAt, cấp access + refresh token.
     */
    @Override
    @Transactional(noRollbackFor = AppException.class)
    public AuthResponse login(LoginRequest request, String userAgent, String ipAddress) {
        User user = userRepository.findByEmailIgnoreCase(request.email()).orElse(null);
        if (user == null) {
            passwordEncoder.matches(request.password(), dummyHash);
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        Instant now = clock.instant();
        if (isCurrentlyLocked(user, now)) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        if (user.getLockedUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleFailedPasswordAttempt(user, now);
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);
        User savedUser = userRepository.save(user);

        String rawRefreshToken = SecureTokenGenerator.generate();
        saveRefreshToken(savedUser, rawRefreshToken, UUID.randomUUID(), userAgent, ipAddress, now);

        return buildAuthResponse(savedUser, rawRefreshToken);
    }

    /**
     * Quy trình làm mới (rotation):
     * 1. Băm token nhận được (SHA-256) rồi tìm và khóa dòng token; không có -> INVALID_REFRESH_TOKEN.
     * 2. Token đã bị thu hồi mà vẫn được dùng -> nghi bị đánh cắp: thu hồi CẢ HỌ token, báo REFRESH_TOKEN_REVOKED.
     * 3. Token hết hạn -> INVALID_REFRESH_TOKEN.
     * 4. Tài khoản bị vô hiệu hóa/khóa -> ACCOUNT_DISABLED/ACCOUNT_LOCKED (không xoay vòng).
     * 5. Hợp lệ -> lưu token mới (cùng familyId), thu hồi token cũ và ghi replacedByTokenId, trả cặp token mới.
     */
    @Override
    @Transactional(noRollbackFor = AppException.class)
    public AuthResponse refresh(RefreshTokenRequest request, String userAgent, String ipAddress) {
        Instant now = clock.instant();

        RefreshToken current = refreshTokenRepository
                .findWithLockByTokenHash(TokenHasher.sha256Hex(request.refreshToken()))
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (current.isRevoked()) {
            int revokedCount = refreshTokenRepository.revokeFamily(
                    current.getFamilyId(), now, REVOKED_REASON_REUSE_DETECTED);
            log.warn("Phat hien dung lai refresh token da thu hoi: userId={}, familyId={}, so token bi thu hoi them={}",
                    current.getUser().getId(), current.getFamilyId(), revokedCount);
            throw new AppException(ErrorCode.REFRESH_TOKEN_REVOKED);
        }

        if (!current.getExpiresAt().isAfter(now)) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = current.getUser();
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        String rawRefreshToken = SecureTokenGenerator.generate();
        RefreshToken next = saveRefreshToken(user, rawRefreshToken, current.getFamilyId(), userAgent, ipAddress, now);

        current.setRevoked(true);
        current.setRevokedAt(now);
        current.setRevokedReason(REVOKED_REASON_ROTATED);
        current.setReplacedByTokenId(next.getId());
        current.setLastUsedAt(now);
        refreshTokenRepository.save(current);

        return buildAuthResponse(user, rawRefreshToken);
    }

    /**
     * Đăng xuất một thiết bị (idempotent): thu hồi cả họ token của refresh token gửi lên.
     */
    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        Instant now = clock.instant();
        refreshTokenRepository.findWithLockByTokenHash(TokenHasher.sha256Hex(request.refreshToken()))
                .ifPresent(token -> refreshTokenRepository.revokeFamily(token.getFamilyId(), now, REVOKED_REASON_LOGOUT));
    }

    /**
     * Đăng xuất mọi thiết bị: thu hồi tất cả refresh token còn hiệu lực của người dùng.
     */
    @Override
    @Transactional
    public void logoutAll(UUID userId) {
        refreshTokenRepository.revokeAllActiveByUserId(userId, clock.instant(), REVOKED_REASON_LOGOUT_ALL);
    }

    /**
     * Đổi mật khẩu của người đang đăng nhập.
     */
    @Override
    @Transactional(noRollbackFor = AppException.class)
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        Instant now = clock.instant();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (isCurrentlyLocked(user, now)) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (user.getLockedUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            boolean locked = recordFailedAttempt(user, now);
            throw new AppException(locked ? ErrorCode.ACCOUNT_LOCKED : ErrorCode.INVALID_OLD_PASSWORD);
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.NEW_PASSWORD_SAME_AS_OLD);
        }

        applyNewPassword(user, request.newPassword(), now, REVOKED_REASON_PASSWORD_CHANGED);
    }

    /**
     * Yêu cầu đặt lại mật khẩu: rate limit đồng nhất cho cả email có thật và không có thật để không lộ tài khoản,
     * sinh token ngẫu nhiên lưu hash SHA-256 + hạn dùng + thời điểm yêu cầu trên {@code users}.
     */
    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        Instant now = clock.instant();
        enforceResetCooldown(request.email(), now);

        userRepository.findByEmailIgnoreCase(request.email())
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .filter(user -> user.getResetRequestedAt() == null
                        || !user.getResetRequestedAt().plusMillis(resetCooldownMs).isAfter(now))
                .ifPresent(user -> {
                    String rawToken = SecureTokenGenerator.generate();
                    user.setResetTokenHash(TokenHasher.sha256Hex(rawToken));
                    user.setResetTokenExpiresAt(now.plusMillis(resetTokenTtlMs));
                    user.setResetRequestedAt(now);
                    userRepository.save(user);

                    long expiresMinutes = Math.max(1L, resetTokenTtlMs / 60_000L);
                    mailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), rawToken, expiresMinutes);
                });

        return new MessageResponse(FORGOT_PASSWORD_ACK);
    }

    /**
     * Đặt lại mật khẩu bằng mã reset từ email.
     * {@code noRollbackFor = AppException}: xóa token đã hết hạn vẫn được lưu lại để tránh kiểm tra lặp.
     */
    @Override
    @Transactional(noRollbackFor = AppException.class)
    public void resetPassword(ResetPasswordRequest request) {
        Instant now = clock.instant();
        User user = userRepository.findByResetTokenHash(TokenHasher.sha256Hex(request.token()))
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_RESET_TOKEN));

        if (user.getResetTokenExpiresAt() == null || !user.getResetTokenExpiresAt().isAfter(now)) {
            clearResetToken(user);
            userRepository.save(user);
            throw new AppException(ErrorCode.INVALID_RESET_TOKEN);
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.NEW_PASSWORD_SAME_AS_OLD);
        }

        clearResetToken(user);
        applyNewPassword(user, request.newPassword(), now, REVOKED_REASON_PASSWORD_RESET);
    }

    private void applyNewPassword(User user, String rawNewPassword, Instant now, String revokeReason) {
        user.setPasswordHash(passwordEncoder.encode(rawNewPassword));
        user.setPasswordChangedAt(now);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        refreshTokenRepository.revokeAllActiveByUserId(user.getId(), now, revokeReason);
    }

    private static void clearResetToken(User user) {
        user.setResetTokenHash(null);
        user.setResetTokenExpiresAt(null);
        user.setResetRequestedAt(null);
    }

    private void enforceResetCooldown(String normalizedEmail, Instant now) {
        if (resetCooldownMs == 0) {
            return;
        }
        if (resetCooldownByEmail.size() > MAX_COOLDOWN_ENTRIES) {
            Instant expiryCutoff = now.minusMillis(resetCooldownMs);
            resetCooldownByEmail.entrySet().removeIf(entry -> !entry.getValue().isAfter(expiryCutoff));
        }
        boolean[] rateLimited = {false};
        resetCooldownByEmail.compute(normalizedEmail, (key, lastRequest) -> {
            if (lastRequest != null && lastRequest.plusMillis(resetCooldownMs).isAfter(now)) {
                rateLimited[0] = true;
                return lastRequest;
            }
            return now;
        });
        if (rateLimited[0]) {
            throw new AppException(ErrorCode.TOO_MANY_REQUESTS);
        }
    }

    /** Lưu refresh token mới (chỉ lưu hash SHA-256 của token thô) thuộc họ token {@code familyId}. */
    private RefreshToken saveRefreshToken(User user, String rawRefreshToken, UUID familyId,
                                          String userAgent, String ipAddress, Instant now) {
        return refreshTokenRepository.save(authMapper.toRefreshToken(
                user,
                TokenHasher.sha256Hex(rawRefreshToken),
                familyId,
                now.plusMillis(refreshTtlMs),
                now,
                userAgent,
                ipAddress
        ));
    }

    private AuthResponse buildAuthResponse(User user, String rawRefreshToken) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole());
        return authMapper.toAuthResponse(accessToken, rawRefreshToken, jwtService.getAccessTokenTtlSeconds(), user);
    }

    private static boolean isCurrentlyLocked(User user, Instant now) {
        return user.getStatus() == UserStatus.LOCKED
                || (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now));
    }

    private void handleFailedPasswordAttempt(User user, Instant now) {
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        boolean locked = recordFailedAttempt(user, now);
        throw new AppException(locked ? ErrorCode.ACCOUNT_LOCKED : ErrorCode.INVALID_CREDENTIALS);
    }

    /**
     * Tăng bộ đếm nhập sai và lưu lại; đạt {@value #MAX_FAILED_ATTEMPTS} lần thì khóa tạm {@link #LOCK_DURATION}.
     */
    private boolean recordFailedAttempt(User user, Instant now) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        boolean locked = attempts >= MAX_FAILED_ATTEMPTS;
        if (locked) {
            user.setLockedUntil(now.plus(LOCK_DURATION));
        }
        userRepository.save(user);
        return locked;
    }
}
