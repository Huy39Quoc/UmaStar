package umastar.storage;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Cac dinh dang file duoc phep. Kiem tra 3 lop: duoi file, MIME khai bao, va quan trong nhat la
 * "chu ky" (magic bytes) o dau file, vi duoi va MIME do client tu khai bao nen co the gia mao.
 */
public enum AllowedFileType {

    PDF("application/pdf", Set.of(), "pdf"),
    JPEG("image/jpeg", Set.of("image/jpg", "image/pjpeg"), "jpg", "jpeg"),
    PNG("image/png", Set.of(), "png");

    /** So byte dau file can doc de doi chieu chu ky (PNG can 8 byte). */
    public static final int SIGNATURE_BYTES = 8;

    private static final String OCTET_STREAM = "application/octet-stream";

    private final String mimeType;
    private final Set<String> mimeAliases;
    private final List<String> extensions;

    AllowedFileType(String mimeType, Set<String> mimeAliases, String... extensions) {
        this.mimeType = mimeType;
        this.mimeAliases = mimeAliases;
        this.extensions = List.of(extensions);
    }

    public String getMimeType() {
        return mimeType;
    }

    /** Duoi file chuan dung khi luu (jpeg luon luu thanh jpg). */
    public String getStoredExtension() {
        return extensions.get(0);
    }

    public static Optional<AllowedFileType> fromExtension(String extension) {
        if (extension == null) {
            return Optional.empty();
        }
        String ext = extension.toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(t -> t.extensions.contains(ext)).findFirst();
    }

    /**
     * MIME do client gui len. Chap nhan MIME chuan, ten goi khac cua no, hoac application/octet-stream / rong
     * (nhieu app mobile gui nhu vay); khi do viec xac thuc dua hoan toan vao chu ky file.
     */
    public boolean acceptsDeclaredMime(String declared) {
        if (declared == null || declared.isBlank()) {
            return true;
        }
        String mime = declared.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        return mime.equals(mimeType) || mimeAliases.contains(mime) || mime.equals(OCTET_STREAM);
    }

    public boolean matchesSignature(byte[] header) {
        return switch (this) {
            case PDF -> startsWith(header, 0x25, 0x50, 0x44, 0x46, 0x2D);                     // %PDF-
            case JPEG -> startsWith(header, 0xFF, 0xD8, 0xFF);
            case PNG -> startsWith(header, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
        };
    }

    private static boolean startsWith(byte[] data, int... expected) {
        if (data == null || data.length < expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if ((data[i] & 0xFF) != expected[i]) {
                return false;
            }
        }
        return true;
    }
}