package umastar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import umastar.dto.request.LoginRequest;
import umastar.dto.request.RegisterRequest;
import umastar.dto.response.AuthResponse;
import umastar.dto.response.RegisterResponse;
import umastar.service.AuthService;

/** API xác thực & tài khoản. Chỉ nhận/trả DTO, mọi nghiệp vụ nằm ở {@link AuthService}. */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Đăng ký, đăng nhập và quản lý phiên đăng nhập")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Đăng ký tài khoản khách hàng (công khai): tạo User CUSTOMER kèm hồ sơ Customer. */
    @Operation(summary = "Đăng ký tài khoản khách hàng")
    @SecurityRequirements
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /** Đăng nhập bằng email & mật khẩu (công khai): trả access token JWT và refresh token. */
    @Operation(summary = "Đăng nhập")
    @SecurityRequirements
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authService.login(
                request,
                httpRequest.getHeader(HttpHeaders.USER_AGENT),
                httpRequest.getRemoteAddr()
        );
    }
}
