package umastar.service;

import umastar.dto.request.CreateUserRequest;
import umastar.dto.request.UpdateProfileRequest;
import umastar.dto.request.UpdateUserStatusRequest;
import umastar.dto.response.PageResponse;
import umastar.dto.response.UserResponse;
import umastar.enums.Role;
import umastar.enums.UserStatus;

import java.util.UUID;

/** Nghiệp vụ hồ sơ cá nhân (/profile) và quản lý người dùng (/users). */
public interface UserService {

    /** Lấy thông tin hồ sơ của chính người dùng đang đăng nhập. */
    UserResponse getProfile(UUID userId);

    /** Cập nhật họ tên và số điện thoại cho chính người dùng đang đăng nhập (không sửa email/role). */
    UserResponse updateProfile(UUID userId, UpdateProfileRequest request);

    /** Tạo tài khoản nhân viên nội bộ (không cho phép tạo role CUSTOMER tại đây). */
    UserResponse createUser(CreateUserRequest request);

    /** Lấy chi tiết một người dùng theo ID. */
    UserResponse getUser(UUID userId);

    /** Tìm kiếm / phân trang danh sách người dùng theo role, status, từ khóa (họ tên hoặc email). */
    PageResponse<UserResponse> listUsers(Role role, UserStatus status, String keyword,
                                         Integer page, Integer size, String sort);

    /**
     * Cập nhật trạng thái tài khoản: cấm tự khóa mình và cấm khóa Logistics Manager đang hoạt động cuối cùng.
     * Khi chuyển sang trạng thái khác ACTIVE sẽ thu hồi toàn bộ refresh token.
     */
    UserResponse updateUserStatus(UUID currentUserId, UUID targetUserId, UpdateUserStatusRequest request);
}
