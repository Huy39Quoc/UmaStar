package umastar.mapper;

import org.springframework.stereotype.Component;
import umastar.dto.request.RegisterRequest;
import umastar.dto.response.RegisterResponse;
import umastar.entity.Customer;
import umastar.entity.User;
import umastar.enums.Role;
import umastar.enums.UserStatus;

/**
 * Chuyển đổi DTO ↔ Entity cho module xác thực & tài khoản ({@code auth}).
 */
@Component
public class AuthMapper {

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
}

