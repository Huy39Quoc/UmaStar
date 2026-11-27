package umastar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import umastar.dto.request.UpdateProfileRequest;
import umastar.dto.response.UserResponse;
import umastar.security.SecurityUtils;
import umastar.service.UserService;

/** API xem và cập nhật hồ sơ cá nhân của người dùng đang đăng nhập. */
@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "Profile", description = "Xem và sửa hồ sơ cá nhân")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    /** Xem thông tin tài khoản của chính người dùng đang đăng nhập. */
    @Operation(summary = "Xem hồ sơ cá nhân")
    @GetMapping
    public UserResponse getProfile() {
        return userService.getProfile(SecurityUtils.getCurrentUserId());
    }

    /** Cập nhật họ tên và số điện thoại cá nhân (không cho phép sửa email và role). */
    @Operation(summary = "Cập nhật hồ sơ cá nhân")
    @PutMapping
    public UserResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(SecurityUtils.getCurrentUserId(), request);
    }
}

