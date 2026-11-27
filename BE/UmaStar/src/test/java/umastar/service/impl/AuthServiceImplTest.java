package umastar.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import umastar.dto.request.RegisterRequest;
import umastar.dto.response.RegisterResponse;
import umastar.entity.Customer;
import umastar.entity.User;
import umastar.enums.CustomerType;
import umastar.enums.Role;
import umastar.enums.UserStatus;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;
import umastar.mapper.AuthMapper;
import umastar.repository.CustomerRepository;
import umastar.repository.UserRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CustomerRepository customerRepository = mock(CustomerRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AuthMapper authMapper = new AuthMapper();
    private final AuthServiceImpl authService =
            new AuthServiceImpl(userRepository, customerRepository, passwordEncoder, authMapper);

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
}

