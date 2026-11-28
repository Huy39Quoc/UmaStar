package umastar.storage;

/**
 * Kết quả lưu file.
 * @param path         đường dẫn lưu trong DB ({@code file_url} / {@code photo_url}): URL Cloudinary {@code https://...} hoặc đường dẫn tương đối local
 * @param originalName tên file gốc người dùng tải lên (chỉ để hiển thị)
 * @param contentType  MIME đã được xác thực theo nội dung file
 * @param size         dung lượng (byte)
 */
public record StoredFile(String path, String originalName, String contentType, long size) {
}