package umastar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import umastar.dto.request.CreateUserRequest;
import umastar.dto.request.UpdateUserStatusRequest;
import umastar.dto.response.PageResponse;
import umastar.dto.response.UserResponse;
import umastar.enums.Role;
import umastar.enums.UserStatus;
import umastar.security.SecurityUtils;
import umastar.service.UserService;

import java.util.UUID;

/** API quản lý tài khoản nhân viên & người dùng (chỉ dành cho Logistics Manager). */
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('LOGISTICS_MANAGER')")
@Tag(name = "Users", description = "Quản lý tài khoản nhân viên — [Chỉ LOGISTICS_MANAGER]")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Tạo tài khoản nhân viên mới (không dùng cho CUSTOMER). */
    @Operation(summary = "[LOGISTICS_MANAGER] Tạo tài khoản nhân viên",
            description = "Quyền truy cập: Chỉ LOGISTICS_MANAGER (tạo tài khoản nhân viên nội bộ, không tạo CUSTOMER).")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    /** Danh sách người dùng có lọc theo role, status, từ khóa và phân trang. */
    @Operation(summary = "[LOGISTICS_MANAGER] Danh sách người dùng",
            description = "Quyền truy cập: Chỉ LOGISTICS_MANAGER (lọc theo role, status, từ khóa và phân trang).")
    @GetMapping
    public PageResponse<UserResponse> listUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return userService.listUsers(role, status, keyword, page, size, sort);
    }

    /** Xem chi tiết một tài khoản theo ID. */
    @Operation(summary = "[LOGISTICS_MANAGER] Chi tiết người dùng",
            description = "Quyền truy cập: Chỉ LOGISTICS_MANAGER.")
    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable UUID id) {
        return userService.getUser(id);
    }

    /** Khóa, vô hiệu hóa hoặc kích hoạt lại tài khoản người dùng. */
    @Operation(summary = "[LOGISTICS_MANAGER] Cập nhật trạng thái tài khoản",
            description = "Quyền truy cập: Chỉ LOGISTICS_MANAGER (khóa/mở khóa tài khoản; không tự vô hiệu hóa mình hoặc Manager cuối cùng).")
    @PatchMapping("/{id}/status")
    public UserResponse updateUserStatus(@PathVariable UUID id,
                                         @Valid @RequestBody UpdateUserStatusRequest request) {
        return userService.updateUserStatus(SecurityUtils.getCurrentUserId(), id, request);
    }
}

