package umastar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umastar.dto.request.LoginRequest;
import umastar.dto.request.RegisterRequest;
import umastar.dto.response.AuthResponse;
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
import umastar.util.SecureTokenGenerator;
import umastar.util.TokenHasher;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    static final int MAX_FAILED_ATTEMPTS = 5;
    static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final long refreshTtlMs;
    private final Clock clock;
    private final String dummyHash;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository,
                           CustomerRepository customerRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           AuthMapper authMapper,
                           @Value("${app.jwt.refresh-exp-ms}") long refreshTtlMs) {
        this(userRepository, customerRepository, refreshTokenRepository, passwordEncoder,
                jwtService, authMapper, refreshTtlMs, Clock.systemUTC());
    }

    AuthServiceImpl(UserRepository userRepository,
                    CustomerRepository customerRepository,
                    RefreshTokenRepository refreshTokenRepository,
                    PasswordEncoder passwordEncoder,
                    JwtService jwtService,
                    AuthMapper authMapper,
                    long refreshTtlMs,
                    Clock clock) {
        if (refreshTtlMs <= 0) {
            throw new IllegalStateException("JWT_REFRESH_EXP_MS phai lon hon 0");
        }
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authMapper = authMapper;
        this.refreshTtlMs = refreshTtlMs;
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

        String accessToken = jwtService.generateAccessToken(savedUser.getId(), savedUser.getRole());
        String rawRefreshToken = SecureTokenGenerator.generate();
        RefreshToken refreshToken = authMapper.toRefreshToken(
                savedUser,
                TokenHasher.sha256Hex(rawRefreshToken),
                UUID.randomUUID(),
                now.plusMillis(refreshTtlMs),
                now,
                userAgent,
                ipAddress
        );
        refreshTokenRepository.save(refreshToken);

        return authMapper.toAuthResponse(accessToken, rawRefreshToken, jwtService.getAccessTokenTtlSeconds(), savedUser);
    }

    private static boolean isCurrentlyLocked(User user, Instant now) {
        return user.getStatus() == UserStatus.LOCKED
                || (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now));
    }

    private void handleFailedPasswordAttempt(User user, Instant now) {
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(now.plus(LOCK_DURATION));
            userRepository.save(user);
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }
        userRepository.save(user);
        throw new AppException(ErrorCode.INVALID_CREDENTIALS);
    }
}
