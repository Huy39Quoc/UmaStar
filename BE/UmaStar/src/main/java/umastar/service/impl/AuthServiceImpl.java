package umastar.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umastar.dto.request.RegisterRequest;
import umastar.dto.response.RegisterResponse;
import umastar.entity.Customer;
import umastar.entity.User;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;
import umastar.mapper.AuthMapper;
import umastar.repository.CustomerRepository;
import umastar.repository.UserRepository;
import umastar.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;

    public AuthServiceImpl(UserRepository userRepository,
                           CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder,
                           AuthMapper authMapper) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authMapper = authMapper;
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
}
