package umastar.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;
import umastar.config.SeedProperties.AccountCredentials;
import umastar.entity.Customer;
import umastar.entity.User;
import umastar.enums.CustomerType;
import umastar.enums.Role;
import umastar.enums.UserStatus;
import umastar.repository.CustomerRepository;
import umastar.repository.UserRepository;
import umastar.util.PasswordPolicy;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Tao tai khoan mac dinh cho tung Role tu .env khi he thong chua co user nao thuoc role do.
 * - Moi role chi seed neu duoc cau hinh du email & password trong .env (de trong se tu bo qua).
 * - Rieng role CUSTOMER se tao kem ho so Customer (1-1) de dat don/them ngua duoc ngay.
 * - An toan khi khoi dong lai: da co user cua role do thi bo qua. Khong ghi mat khau ra log.
 * - User va ho so Customer duoc luu trong CUNG MOT transaction: loi giua chung thi khong de lai user mo coi
 *   (neu khong, lan khoi dong sau thay da co role CUSTOMER se bo qua va khong bao gio tao lai ho so).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+$");

    private static final Map<Role, String> DEFAULT_FULL_NAMES = Map.of(
            Role.LOGISTICS_MANAGER, "Quản lý Điều hành Logistics",
            Role.TRANSPORT_SPECIALIST, "Chuyên viên Thủ tục & Kiểm dịch",
            Role.FLEET_COORDINATOR, "Điều phối viên Đội xe & Lộ trình",
            Role.DRIVER, "Tài xế Vận chuyển",
            Role.ESCORT, "Nhân viên Đi kèm Ngựa",
            Role.CUSTOMER, "Khách hàng CLB Mẫu"
    );

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final SeedProperties seedProperties;
    private final TransactionOperations transactionOperations;

    public DataSeeder(UserRepository userRepository,
                      CustomerRepository customerRepository,
                      PasswordEncoder passwordEncoder,
                      SeedProperties seedProperties,
                      TransactionOperations transactionOperations) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedProperties = seedProperties;
        this.transactionOperations = transactionOperations;
    }

    @Override
    public void run(String... args) {
        seedAllRoles();
    }

    /** @return so tai khoan vua duoc tao moi. */
    int seedAllRoles() {
        int created = 0;
        for (Role role : Role.values()) {
            AccountCredentials creds = seedProperties.getAccounts().get(role);
            if (creds != null && seedRoleAccount(role, creds.getEmail(), creds.getPassword())) {
                created++;
            }
        }
        return created;
    }

    boolean seedRoleAccount(Role role, String rawEmail, String rawPassword) {
        if (userRepository.existsByRole(role)) {
            log.debug("Da co tai khoan cho role {}, bo qua seed", role);
            return false;
        }
        if (isBlank(rawEmail) || isBlank(rawPassword)) {
            if (role == Role.LOGISTICS_MANAGER) {
                log.warn("Chua co Manager va chua cau hinh SEED_MANAGER_EMAIL / SEED_MANAGER_PASSWORD");
            }
            return false;
        }

        String email = rawEmail.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(email).matches()) {
            throw new IllegalStateException("Email seed cho role " + role + " khong hop le: " + email);
        }
        if (!PasswordPolicy.isValid(rawPassword)) {
            throw new IllegalStateException("Mat khau seed cho role " + role + " khong dat chinh sach: "
                    + PasswordPolicy.REQUIREMENT);
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            log.error("Email {} da duoc dung boi tai khoan khac, khong the seed cho role {}", email, role);
            return false;
        }

        User user = new User();
        user.setFullName(DEFAULT_FULL_NAMES.getOrDefault(role, role.name()));
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);

        try {
            // Goi qua TransactionOperations (khong dung @Transactional) vi seedRoleAccount duoc goi noi bo trong cung bean,
            // proxy cua Spring se khong can thiep.
            transactionOperations.executeWithoutResult(status -> {
                User saved = userRepository.save(user);
                if (role == Role.CUSTOMER) {
                    Customer customer = new Customer();
                    customer.setUser(saved);
                    customer.setOrganizationName("CLB Ngựa Đua UmaStar (Demo)");
                    customer.setCustomerType(CustomerType.CLUB);
                    customerRepository.save(customer);
                }
            });
        } catch (DataIntegrityViolationException e) {
            log.warn("Tai khoan seed cho role {} da duoc tao boi tien trinh khac, bo qua", role);
            return false;
        }

        log.info("Da tao tai khoan mac dinh cho role {}: {} (hay doi mat khau sau khi dang nhap)", role, email);
        return true;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}