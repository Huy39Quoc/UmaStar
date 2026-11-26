package umastar.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class SecureTokenGenerator {

    public static final int DEFAULT_BYTES = 32;
    public static final int MIN_BYTES = 16;

    // SecureRandom an toan khi dung chung giua nhieu thread
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private SecureTokenGenerator() {
    }

    public static String generate() {
        return generate(DEFAULT_BYTES);
    }

    public static String generate(int numBytes) {
        if (numBytes < MIN_BYTES) {
            throw new IllegalArgumentException("Token phai co it nhat " + MIN_BYTES + " byte");
        }
        byte[] bytes = new byte[numBytes];
        RANDOM.nextBytes(bytes);
        return ENCODER.encodeToString(bytes);
    }
}