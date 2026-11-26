package umastar.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import umastar.enums.Role;

import java.util.EnumMap;
import java.util.Map;

/**
 * Cau hinh danh sach tai khoan seed theo tung Role tu application.properties / .env.
 */
@Component
@ConfigurationProperties(prefix = "app.seed")
@Getter
@Setter
public class SeedProperties {

    private Map<Role, AccountCredentials> accounts = new EnumMap<>(Role.class);

    @Getter
    @Setter
    public static class AccountCredentials {
        private String email;
        private String password;
    }
}

