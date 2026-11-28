package umastar.service.impl;

import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umastar.dto.request.CreateUserRequest;
import umastar.dto.request.UpdateProfileRequest;
import umastar.dto.request.UpdateUserStatusRequest;
import umastar.dto.response.PageResponse;
import umastar.dto.response.UserResponse;
import umastar.entity.User;
import umastar.enums.Role;
import umastar.enums.UserStatus;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;
import umastar.mapper.UserMapper;
import umastar.repository.RefreshTokenRepository;
import umastar.repository.UserRepository;
import umastar.service.UserService;
import umastar.util.InputNormalizer;
import umastar.util.PageableFactory;
import umastar.util.SqlLike;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    static final String REVOKED_REASON_STATUS_CHANGED = "USER_STATUS_CHANGED";
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "fullName", "email", "role", "status");

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final Clock clock;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder,
                           UserMapper userMapper) {
        this(userRepository, refreshTokenRepository, passwordEncoder, userMapper, Clock.systemUTC());
    }

    UserServiceImpl(UserRepository userRepository,
                    RefreshTokenRepository refreshTokenRepository,
                    PasswordEncoder passwordEncoder,
                    UserMapper userMapper,
                    Clock clock) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        return userMapper.toUserResponse(findUserOrThrow(userId));
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = findUserOrThrow(userId);
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (request.role() == Role.CUSTOMER) {
            throw new AppException(ErrorCode.INVALID_STAFF_ROLE);
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        User saved = userRepository.save(
                userMapper.toStaffUser(request, passwordEncoder.encode(request.password())));
        return userMapper.toUserResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(UUID userId) {
        return userMapper.toUserResponse(findUserOrThrow(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(Role role, UserStatus status, String keyword,
                                                Integer page, Integer size, String sort) {
        Pageable pageable = PageableFactory.of(
                page, size, sort, ALLOWED_SORT_FIELDS, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> result = userRepository.findAll(buildSearchSpec(role, status, keyword), pageable);
        return PageResponse.from(result, userMapper::toUserResponse);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(UUID currentUserId, UUID targetUserId, UpdateUserStatusRequest request) {
        User target = findUserOrThrow(targetUserId);
        UserStatus newStatus = request.status();

        if (targetUserId.equals(currentUserId) && newStatus != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.CANNOT_DEACTIVATE_SELF);
        }
        if (target.getRole() == Role.LOGISTICS_MANAGER
                && target.getStatus() == UserStatus.ACTIVE
                && newStatus != UserStatus.ACTIVE
                && userRepository.countByRoleAndStatus(Role.LOGISTICS_MANAGER, UserStatus.ACTIVE) <= 1) {
            throw new AppException(ErrorCode.LAST_MANAGER_PROTECTED);
        }

        target.setStatus(newStatus);
        if (newStatus == UserStatus.ACTIVE) {
            target.setFailedLoginAttempts(0);
            target.setLockedUntil(null);
        } else {
            refreshTokenRepository.revokeAllActiveByUserId(targetUserId, clock.instant(), REVOKED_REASON_STATUS_CHANGED);
        }

        return userMapper.toUserResponse(userRepository.save(target));
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private static Specification<User> buildSearchSpec(Role role, UserStatus status, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (role != null) {
                predicates.add(cb.equal(root.get("role"), role));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            String cleanKeyword = InputNormalizer.blankToNull(keyword);
            if (cleanKeyword != null) {
                String pattern = SqlLike.contains(cleanKeyword);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), pattern, SqlLike.ESCAPE),
                        cb.like(cb.lower(root.get("email")), pattern, SqlLike.ESCAPE)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}

