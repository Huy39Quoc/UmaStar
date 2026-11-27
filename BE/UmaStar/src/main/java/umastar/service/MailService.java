package umastar.service;

/**
 * Dịch vụ gửi email thông báo/xác thực. Có thể thay thế triển khai bằng SMTP thật mà không đổi tầng nghiệp vụ.
 */
public interface MailService {

    /**
     * Gửi mã đặt lại mật khẩu đến email người dùng.
     *
     * @param toEmail địa chỉ email nhận
     * @param fullName tên người dùng
     * @param rawToken mã thô gửi cho người dùng (chỉ dùng 1 lần)
     * @param expiresInMinutes thời hạn hiệu lực (phút)
     */
    void sendPasswordResetEmail(String toEmail, String fullName, String rawToken, long expiresInMinutes);
}

