package umastar.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import umastar.service.MailService;

/**
 * Triển khai {@link MailService} cho môi trường phát triển (ghi log thay vì gửi SMTP thật).
 */
@Service
public class MailServiceImpl implements MailService {

    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

    @Override
    public void sendPasswordResetEmail(String toEmail, String fullName, String rawToken, long expiresInMinutes) {
        log.info("[DEV MAIL] Gui email dat lai mat khau toi {} ({}), hieu luc {} phut. Token reset: {}",
                toEmail, fullName, expiresInMinutes, rawToken);
    }
}

