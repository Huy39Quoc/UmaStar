package umastar.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;
import umastar.dto.response.ErrorResponse;
import umastar.exception.ErrorCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Cau hinh bao mat cho API (dung JWT stateless, khong dung session/form login).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // Cac duong dan mo cong khai khong can dang nhap
    private static final String[] PUBLIC_ENDPOINTS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/v1/auth/**"
    };

    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Tat CSRF va Session vi REST API xac thuc bang JWT o moi request
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Cho phep Swagger va /auth/** truy cap tu do; moi API khac bat buoc dang nhap
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                // Khi chua dang nhap (401) hoac khong du quyen (403) -> tra JSON ErrorResponse thay vi trang HTML
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> writeError(res, ErrorCode.UNAUTHORIZED, req.getRequestURI()))
                        .accessDeniedHandler((req, res, e) -> writeError(res, ErrorCode.FORBIDDEN, req.getRequestURI()))
                )
                .build();
    }

    // Ma hoa mat khau bang BCrypt (dung khi dang ky / doi mat khau / kiem tra dang nhap)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void writeError(HttpServletResponse res, ErrorCode code, String path) throws IOException {
        res.setStatus(code.getStatus());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.of(code, code.getDefaultMessage(), path)));
        res.getWriter().flush();
    }
}
