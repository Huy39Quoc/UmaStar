package umastar.dto.response;

/**
 * Bộ token trả về sau khi đăng nhập (và sau này khi làm mới token).
 * {@code expiresIn} là thời gian sống của accessToken tính bằng giây. refreshToken chỉ xuất hiện ĐÚNG MỘT LẦN ở đây:
 * DB chỉ lưu hash nên hệ thống không thể cấp lại bản gốc.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserSummaryResponse user) {

    public static final String BEARER = "Bearer";

    /** Che token để lỡ ghi log đối tượng này cũng không lộ phiên đăng nhập. */
    @Override
    public String toString() {
        return "AuthResponse[tokenType=" + tokenType + ", expiresIn=" + expiresIn + ", user=" + user + ", tokens=***]";
    }
}
