package umastar.config;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import umastar.config.SeedProperties.AccountCredentials;
import umastar.entity.Customer;
import umastar.entity.User;
import umastar.enums.CustomerType;
import umastar.enums.Role;
import umastar.enums.UserStatus;
import umastar.repository.CustomerRepository;
import umastar.repository.UserRepository;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataSeederTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CustomerRepository customerRepository = mock(CustomerRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SeedProperties props = new SeedProperties();

    private DataSeeder seeder() {
        return new DataSeeder(userRepository, customerRepository, passwordEncoder, props,
                TransactionOperations.withoutTransaction());
    }

    private void configure(Role role, String email, String password) {
        AccountCredentials creds = new AccountCredentials();
        creds.setEmail(email);
        creds.setPassword(password);
        props.getAccounts().put(role, creds);
    }

    private void nothingExistsYet() {
        when(userRepository.existsByRole(any())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void skipsWhenRoleAlreadyExists() {
        when(userRepository.existsByRole(Role.LOGISTICS_MANAGER)).thenReturn(true);
        configure(Role.LOGISTICS_MANAGER, "manager@umastar.local", "ChangeMe@123");

        assertEquals(0, seeder().seedAllRoles());
        verify(userRepository, never()).save(any());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void createsManagerAndCustomerWithLinkedProfile() {
        nothingExistsYet();
        configure(Role.LOGISTICS_MANAGER, "  Manager@UmaStar.local ", "ChangeMe@123");
        configure(Role.CUSTOMER, "customer@umastar.local", "ChangeMe@123");

        assertEquals(2, seeder().seedAllRoles());

        verify(userRepository).save(argThat(u ->
                u.getEmail().equals("manager@umastar.local")
                        && u.getRole() == Role.LOGISTICS_MANAGER
                        && u.getStatus() == UserStatus.ACTIVE
                        && u.isEmailVerified()
                        && passwordEncoder.matches("ChangeMe@123", u.getPasswordHash())));
        verify(customerRepository, times(1)).save(argThat((Customer c) ->
                c.getUser().getEmail().equals("customer@umastar.local")
                        && c.getCustomerType() == CustomerType.CLUB));
    }

    @Test
    void nonCustomerRolesDoNotGetCustomerProfile() {
        nothingExistsYet();
        configure(Role.DRIVER, "driver@umastar.local", "ChangeMe@123");

        assertEquals(1, seeder().seedAllRoles());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void skipsRolesWithBlankOrMissingCredentials() {
        nothingExistsYet();
        configure(Role.ESCORT, "", "ChangeMe@123");
        configure(Role.DRIVER, "driver@umastar.local", "   ");

        assertEquals(0, seeder().seedAllRoles());
        verify(userRepository, never()).save(any());
    }

    @Test
    void failsFastOnBadEmailOrWeakPassword() {
        nothingExistsYet();
        configure(Role.LOGISTICS_MANAGER, "khong-phai-email", "ChangeMe@123");
        assertThrows(IllegalStateException.class, () -> seeder().seedAllRoles());

        props.getAccounts().clear();
        configure(Role.LOGISTICS_MANAGER, "manager@umastar.local", "yeu");
        assertThrows(IllegalStateException.class, () -> seeder().seedAllRoles());
        verify(userRepository, never()).save(any());
    }

    @Test
    void doesNotTouchAnAccountThatAlreadyUsesTheEmail() {
        nothingExistsYet();
        when(userRepository.existsByEmailIgnoreCase("manager@umastar.local")).thenReturn(true);
        configure(Role.LOGISTICS_MANAGER, "manager@umastar.local", "ChangeMe@123");

        assertEquals(0, seeder().seedAllRoles());
        verify(userRepository, never()).save(any());
    }

    @Test
    void survivesRaceWhenAnotherInstanceInsertedFirst() {
        nothingExistsYet();
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));
        configure(Role.LOGISTICS_MANAGER, "manager@umastar.local", "ChangeMe@123");

        assertEquals(0, seeder().seedAllRoles());
    }

    @Test
    void userAndCustomerProfileAreSavedInsideTheSameTransaction() {
        AtomicBoolean inTransaction = new AtomicBoolean();
        TransactionOperations tx = new TransactionOperations() {
            @Override
            public <T> T execute(TransactionCallback<T> action) {
                inTransaction.set(true);
                try {
                    return action.doInTransaction(null);
                } finally {
                    inTransaction.set(false);
                }
            }
        };
        when(userRepository.existsByRole(any())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            assertTrue(inTransaction.get(), "User phai duoc luu trong transaction");
            return inv.getArgument(0);
        });
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            assertTrue(inTransaction.get(), "Customer phai duoc luu trong transaction");
            return inv.getArgument(0);
        });
        configure(Role.CUSTOMER, "customer@umastar.local", "ChangeMe@123");

        assertEquals(1, new DataSeeder(userRepository, customerRepository, passwordEncoder, props, tx).seedAllRoles());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }
}