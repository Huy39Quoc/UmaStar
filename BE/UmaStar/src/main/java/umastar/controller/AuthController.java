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
import umastar.dto.request.ChangePasswordRequest;
import umastar.dto.request.ForgotPasswordRequest;
import umastar.dto.request.LoginRequest;
import umastar.dto.request.RefreshTokenRequest;
import umastar.dto.request.RegisterRequest;
import umastar.dto.request.ResetPasswordRequest;
import umastar.dto.response.AuthResponse;
import umastar.dto.response.MessageResponse;
import umastar.dto.response.RegisterResponse;
import umastar.security.SecurityUtils;
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

    /** Làm mới phiên (công khai): đổi refresh token cũ lấy cặp token mới; token cũ bị thu hồi ngay. */
    @Operation(summary = "Làm mới access token (xoay vòng refresh token)")
    @SecurityRequirements
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        return authService.refresh(
                request,
                httpRequest.getHeader(HttpHeaders.USER_AGENT),
                httpRequest.getRemoteAddr()
        );
    }

    /**
     * Đăng xuất khỏi một thiết bị (công khai): thu hồi họ refresh token được gửi lên.
     * Không cần access token nên vẫn đăng xuất được khi access token đã hết hạn.
     */
    @Operation(summary = "Đăng xuất khỏi thiết bị hiện tại")
    @SecurityRequirements
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
    }

    /** Đăng xuất khỏi tất cả thiết bị (cần access token): thu hồi mọi refresh token còn hiệu lực của người dùng. */
    @Operation(summary = "Đăng xuất khỏi tất cả thiết bị")
    @PostMapping("/logout-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logoutAll() {
        authService.logoutAll(SecurityUtils.getCurrentUserId());
    }

    /**
     * Đổi mật khẩu (cần access token): kiểm tra mật khẩu hiện tại, mật khẩu mới phải khác và đạt chính sách.
     * Thành công thì mọi phiên đăng nhập (kể cả thiết bị này) bị thu hồi, client cần đăng nhập lại.
     */
    @Operation(summary = "Đổi mật khẩu")
    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(SecurityUtils.getCurrentUserId(), request);
    }

    /** Yêu cầu đặt lại mật khẩu qua email (công khai): luôn trả 200 OK nếu chưa vượt rate limit. */
    @Operation(summary = "Quên mật khẩu")
    @SecurityRequirements
    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    /** Đặt lại mật khẩu bằng token nhận được qua email (công khai). */
    @Operation(summary = "Đặt lại mật khẩu")
    @SecurityRequirements
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
    }
}
