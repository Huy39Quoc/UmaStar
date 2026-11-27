package umastar.util;

import java.util.Locale;

/** Chuẩn hóa chuỗi đầu vào dùng chung cho các DTO request. */
public final class InputNormalizer {

    private InputNormalizer() {
    }

    public static String trim(String value) {
        return value == null ? null : value.trim();
    }

    public static String blankToNull(String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }

    public static String normalizeEmail(String value) {
        String trimmed = trim(value);
        return trimmed == null ? null : trimmed.toLowerCase(Locale.ROOT);
    }
}

