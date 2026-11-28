package umastar.storage;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Loai file nghiep vu: quyet dinh thu muc luu, dung luong toi da va dinh dang cho phep.
 * Gioi han o day KHONG duoc lon hon MAX_FILE_SIZE trong .env (Spring chan truoc khi toi day).
 */
public enum FileCategory {

    /** Giay to phap ly (Flow 2). */
    DOCUMENT("documents", 10, EnumSet.of(AllowedFileType.PDF, AllowedFileType.JPEG, AllowedFileType.PNG)),
    /** Anh ngua truoc van chuyen (Flow 1), anh nhat ky suc khoe (Flow 4), su co (Flow 5), ban giao (Flow 6). */
    IMAGE("images", 5, EnumSet.of(AllowedFileType.JPEG, AllowedFileType.PNG)),
    /** Chu ky nguoi nhan khi ban giao (Flow 6). */
    SIGNATURE("signatures", 2, EnumSet.of(AllowedFileType.JPEG, AllowedFileType.PNG));

    private final String directory;
    private final long maxBytes;
    private final Set<AllowedFileType> allowedTypes;

    FileCategory(String directory, int maxMegabytes, Set<AllowedFileType> allowedTypes) {
        this.directory = directory;
        this.maxBytes = maxMegabytes * 1024L * 1024L;
        this.allowedTypes = allowedTypes;
    }

    public String getDirectory() {
        return directory;
    }

    public long getMaxBytes() {
        return maxBytes;
    }

    public long getMaxMegabytes() {
        return maxBytes / (1024L * 1024L);
    }

    public boolean allows(AllowedFileType type) {
        return allowedTypes.contains(type);
    }

    /** Vd: "PDF, JPG, PNG" - dung trong thong bao loi. */
    public String allowedExtensionsText() {
        return allowedTypes.stream()
                .map(t -> t.getStoredExtension().toUpperCase())
                .collect(Collectors.joining(", "));
    }
}