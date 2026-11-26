package umastar.util;

import java.nio.charset.StandardCharsets;

/**
 * Chinh sach mat khau dung chung (seed Manager, dang ky, doi mat khau, dat lai mat khau):
 * 8 ky tu tro len, co chu hoa, chu thuong, chu so, va toi da 72 byte (gioi han cua BCrypt).
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_BYTES = 72;
    public static final String REQUIREMENT = "Mật khẩu từ 8 ký tự, có chữ hoa, chữ thường và chữ số (tối đa 72 byte)";

    private PasswordPolicy() {
    }

    public static boolean isValid(String password) {
        if (password == null || password.length() < MIN_LENGTH
                || password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            return false;
        }
        boolean upper = password.codePoints().anyMatch(Character::isUpperCase);
        boolean lower = password.codePoints().anyMatch(Character::isLowerCase);
        boolean digit = password.codePoints().anyMatch(Character::isDigit);
        return upper && lower && digit;
    }
}