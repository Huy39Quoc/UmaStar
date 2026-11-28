package umastar.util;

import java.util.Locale;

/** Dựng mẫu tìm kiếm {@code LIKE '%từ khóa%'} an toàn: thoát các ký tự đặc biệt {@code % _ \} để người dùng không "chèn" wildcard. */
public final class SqlLike {

    /** Ký tự thoát phải truyền vào {@code cb.like(expression, pattern, SqlLike.ESCAPE)}. */
    public static final char ESCAPE = '\\';

    private SqlLike() {
    }

    /** Mẫu "chứa" không phân biệt hoa thường; dùng cùng {@code cb.lower(path)}. */
    public static String contains(String keyword) {
        String escaped = keyword.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
