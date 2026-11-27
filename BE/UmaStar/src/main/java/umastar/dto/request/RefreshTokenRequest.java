package umastar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import umastar.util.InputNormalizer;

/**
 * Yêu cầu làm mới phiên đăng nhập bằng refresh token nhận được lúc đăng nhập (hoặc lần làm mới trước).
 * Token được cắt khoảng trắng hai đầu (do sao chép/dán) và không bao giờ được ghi ra log.
 */
public record RefreshTokenRequest(

        @NotBlank(message = "Vui lòng cung cấp refresh token")
        @Size(max = 512, message = "Refresh token không hợp lệ")
        String refreshToken) {

    public RefreshTokenRequest {
        refreshToken = InputNormalizer.trim(refreshToken);
    }

    /** Che token để lỡ ghi log đối tượng này cũng không lộ phiên đăng nhập. */
    @Override
    public String toString() {
        return "RefreshTokenRequest[refreshToken=***]";
    }
}
