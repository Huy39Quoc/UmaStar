package umastar.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;
import umastar.service.FileStorageService;
import umastar.storage.AllowedFileType;
import umastar.storage.FileCategory;
import umastar.storage.StoredFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.YearMonth;
import java.util.Locale;
import java.util.UUID;

/**
 * Triển khai {@link FileStorageService} lưu file lên ổ đĩa local theo cấu trúc:
 * {@code {UPLOAD_DIR}/{loai}/{nam}/{thang}/{uuid}.{duoi}}.
 * Tên file gốc KHÔNG bao giờ được dùng để đặt tên trên đĩa, nên không thể ghi đè file khác hay thoát khỏi thư mục upload.
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class);
    private static final int MAX_ORIGINAL_NAME_LENGTH = 255;

    private final Path baseDir;

    public FileStorageServiceImpl(@Value("${app.upload.dir}") String uploadDir) {
        if (uploadDir == null || uploadDir.isBlank()) {
            throw new IllegalStateException("app.upload.dir khong duoc rong");
        }
        this.baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new IllegalStateException("Khong the tao thu muc upload: " + baseDir, e);
        }
    }

    @Override
    public StoredFile store(MultipartFile file, FileCategory category) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "File tải lên không được rỗng");
        }
        if (file.getSize() > category.getMaxBytes()) {
            throw tooLarge(category);
        }

        String originalName = sanitizeOriginalName(file.getOriginalFilename());
        AllowedFileType type = AllowedFileType.fromExtension(extensionOf(originalName))
                .filter(category::allows)
                .orElseThrow(() -> unsupported(category));
        if (!type.acceptsDeclaredMime(file.getContentType())) {
            throw unsupported(category);
        }
        if (!type.matchesSignature(readHeader(file))) {
            throw new AppException(ErrorCode.UNSUPPORTED_FILE_TYPE,
                    "Nội dung file không đúng với định dạng " + type.getStoredExtension().toUpperCase());
        }

        YearMonth now = YearMonth.now();
        String relativePath = String.format("%s/%d/%02d/%s.%s",
                category.getDirectory(), now.getYear(), now.getMonthValue(),
                UUID.randomUUID(), type.getStoredExtension());
        Path target = resolveInsideBase(relativePath);

        try {
            Files.createDirectories(target.getParent());
            long written;
            try (InputStream in = file.getInputStream()) {
                written = Files.copy(in, target);   // khong ghi de: neu da ton tai se bao loi
            }
            if (written > category.getMaxBytes()) {
                deleteQuietly(target);
                throw tooLarge(category);
            }
            return new StoredFile(relativePath, originalName, type.getMimeType(), written);
        } catch (IOException e) {
            deleteQuietly(target);
            log.error("Khong the luu file {}", relativePath, e);
            throw new AppException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    @Override
    public Resource load(String path) {
        Path file = resolveInsideBase(path);
        try {
            if (!Files.isRegularFile(file)) {
                throw new AppException(ErrorCode.FILE_NOT_FOUND);
            }
            // Chan symlink tro ra ngoai thu muc upload
            Path real = file.toRealPath();
            if (!real.startsWith(baseDir.toRealPath())) {
                throw new AppException(ErrorCode.INVALID_FILE_NAME);
            }
            return new FileSystemResource(real);
        } catch (IOException e) {
            log.error("Khong the doc file {}", path, e);
            throw new AppException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    @Override
    public void delete(String path) {
        Path file = resolveInsideBase(path);
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.error("Khong the xoa file {}", path, e);
            throw new AppException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    // ===================== Ham noi bo =====================

    /** Chong path traversal: duong dan sau khi chuan hoa bat buoc phai nam trong thu muc upload. */
    private Path resolveInsideBase(String relativePath) {
        if (relativePath == null || relativePath.isBlank()
                || relativePath.indexOf('\0') >= 0 || relativePath.indexOf('\\') >= 0) {
            throw new AppException(ErrorCode.INVALID_FILE_NAME);
        }
        try {
            Path resolved = baseDir.resolve(relativePath).normalize();
            if (!resolved.startsWith(baseDir) || resolved.equals(baseDir)) {
                throw new AppException(ErrorCode.INVALID_FILE_NAME);
            }
            return resolved;
        } catch (InvalidPathException e) {
            throw new AppException(ErrorCode.INVALID_FILE_NAME);
        }
    }

    /** Lay ten file (bo phan duong dan neu client gui ca duong dan) va kiem tra hop le. */
    private static String sanitizeOriginalName(String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        name = name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\')) + 1);
        if (name.isBlank() || name.length() > MAX_ORIGINAL_NAME_LENGTH
                || name.chars().anyMatch(c -> c < 32 || c == 127)) {
            throw new AppException(ErrorCode.INVALID_FILE_NAME);
        }
        return name;
    }

    private static String extensionOf(String name) {
        int dot = name.lastIndexOf('.');
        if (dot <= 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static byte[] readHeader(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            return in.readNBytes(AllowedFileType.SIGNATURE_BYTES);
        } catch (IOException e) {
            log.error("Khong the doc noi dung file tai len", e);
            throw new AppException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    private static AppException tooLarge(FileCategory category) {
        return new AppException(ErrorCode.FILE_TOO_LARGE,
                "File vượt quá dung lượng cho phép (tối đa " + category.getMaxMegabytes() + " MB)");
    }

    private static AppException unsupported(FileCategory category) {
        return new AppException(ErrorCode.UNSUPPORTED_FILE_TYPE,
                "Chỉ chấp nhận file định dạng: " + category.allowedExtensionsText());
    }

    private void deleteQuietly(Path target) {
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Khong the don file loi {}", target, e);
        }
    }
}

