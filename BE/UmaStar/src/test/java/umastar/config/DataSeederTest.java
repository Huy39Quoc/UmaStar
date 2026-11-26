package umastar.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import umastar.config.SeedProperties.AccountCredentials;
import umastar.entity.Customer;
import umastar.entity.User;
import umastar.enums.CustomerType;
import umastar.enums.Role;
import umastar.enums.UserStatus;
import umastar.repository.CustomerRepository;
import umastar.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DataSeederTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CustomerRepository customerRepository = mock(CustomerRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void skipsWhenRoleAlreadyExists() {
        when(userRepository.existsByRole(Role.LOGISTICS_MANAGER)).thenReturn(true);

        SeedProperties props = new SeedProperties();
        AccountCredentials creds = new AccountCredentials();
        creds.setEmail("manager@umastar.local");
        creds.setPassword("ChangeMe@123");
        props.getAccounts().put(Role.LOGISTICS_MANAGER, creds);

        DataSeeder seeder = new DataSeeder(userRepository, customerRepository, passwordEncoder, props);
        assertEquals(0, seeder.seedAllRoles());
        verify(userRepository, never()).save(any());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void createsCustomerWithLinkedProfileAndManagerAccount() {
        when(userRepository.existsByRole(any())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        SeedProperties props = new SeedProperties();

        AccountCredentials managerCreds = new AccountCredentials();
        managerCreds.setEmail("  Manager@UmaStar.local ");
        managerCreds.setPassword("ChangeMe@123");
        props.getAccounts().put(Role.LOGISTICS_MANAGER, managerCreds);

        AccountCredentials customerCreds = new AccountCredentials();
        customerCreds.setEmail("customer@umastar.local");
        customerCreds.setPassword("ChangeMe@123");
        props.getAccounts().put(Role.CUSTOMER, customerCreds);

        DataSeeder seeder = new DataSeeder(userRepository, customerRepository, passwordEncoder, props);
        assertEquals(2, seeder.seedAllRoles());

        verify(userRepository).save(argThat(u ->
                u.getEmail().equals("manager@umastar.local")
                        && u.getRole() == Role.LOGISTICS_MANAGER
                        && u.getStatus() == UserStatus.ACTIVE
                        && u.isEmailVerified()
                        && passwordEncoder.matches("ChangeMe@123", u.getPasswordHash())
        ));
        verify(customerRepository, times(1)).save(argThat((Customer c) ->
                c.getUser().getEmail().equals("customer@umastar.local")
                        && c.getCustomerType() == CustomerType.CLUB
        ));
    }
}
