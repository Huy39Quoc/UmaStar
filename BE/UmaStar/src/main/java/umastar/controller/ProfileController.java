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
@Tag(name = "Profile", description = "Xem và sửa hồ sơ cá nhân — [Mọi role đã đăng nhập]")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    /** Xem thông tin tài khoản của chính người dùng đang đăng nhập. */
    @Operation(summary = "[Mọi role đã đăng nhập] Xem hồ sơ cá nhân",
            description = "Quyền truy cập: Đã đăng nhập (mọi role; xem thông tin tài khoản của chính mình).")
    @GetMapping
    public UserResponse getProfile() {
        return userService.getProfile(SecurityUtils.getCurrentUserId());
    }

    /** Cập nhật họ tên và số điện thoại cá nhân (không cho phép sửa email và role). */
    @Operation(summary = "[Mọi role đã đăng nhập] Cập nhật hồ sơ cá nhân",
            description = "Quyền truy cập: Đã đăng nhập (mọi role; sửa họ tên và số điện thoại của chính mình, không sửa được email và role).")
    @PutMapping
    public UserResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(SecurityUtils.getCurrentUserId(), request);
    }
}

