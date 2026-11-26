package umastar.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS cho phep frontend (trinh duyet) goi API. Chi cac origin liet ke trong CORS_ALLOWED_ORIGINS
 * (cach nhau bang dau phay) moi duoc phep; de trong nghia la tu choi moi origin.
 * Khong cho dung "*" va khong bat allowCredentials: xac thuc bang header Authorization (Bearer),
 * khong phu thuoc cookie nen khong can.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:}") String allowedOrigins) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildConfiguration(parseOrigins(allowedOrigins)));
        return source;
    }

    static CorsConfiguration buildConfiguration(List<String> origins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of(
                HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name()));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Content-Disposition"));   // de FE doc ten file khi tai ve
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);                                    // trinh duyet nho ket qua preflight 1 gio
        return config;
    }

    /** Tach, cat khoang trang, bo dau '/' cuoi (trinh duyet gui Origin khong co '/'), bo trung. */
    static List<String> parseOrigins(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        List<String> origins = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.endsWith("/") ? s.substring(0, s.length() - 1) : s)
                .distinct()
                .toList();
        if (origins.contains("*")) {
            throw new IllegalStateException("CORS_ALLOWED_ORIGINS khong duoc dung '*', hay liet ke tung origin");
        }
        return origins;
    }
}