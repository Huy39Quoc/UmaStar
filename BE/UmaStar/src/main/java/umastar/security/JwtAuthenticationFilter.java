package umastar.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;
import umastar.config.SecurityConfig;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;

import java.io.IOException;
import java.util.Arrays;

/**
 * Doc Bearer access token tu header Authorization, xac thuc va gan UserPrincipal vao SecurityContext.
 * - Khong co header Bearer: cho di tiep, Spring Security se tra 401 neu endpoint can dang nhap.
 * - Co token nhung sai/het han/tai khoan bi khoa: tra loi JSON ngay tai day.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   CustomUserDetailsService userDetailsService,
                                   ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return Arrays.stream(SecurityConfig.PUBLIC_ENDPOINTS)
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            authenticate(header.substring(BEARER_PREFIX.length()).trim(), request);
        } catch (AppException ex) {
            SecurityContextHolder.clearContext();
            SecurityConfig.writeError(response, objectMapper, ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
            return;
        }
        // Goi filterChain NGOAI try/catch: loi phat sinh o controller khong bi bo loc nay bat nham thanh loi token.
        filterChain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        AccessTokenClaims claims = jwtService.parse(token);
        UserPrincipal principal = userDetailsService.findById(claims.userId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        validateUserState(principal, claims);

        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void validateUserState(UserPrincipal principal, AccessTokenClaims claims) {
        if (!principal.isEnabled()) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (!principal.isAccountNonLocked()) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (claims.issuedBefore(principal.getPasswordChangedAt())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED, "Phiên đăng nhập đã hết hạn do mật khẩu vừa thay đổi");
        }
    }
}