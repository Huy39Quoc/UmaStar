package umastar.storage;

/**
 * Ket qua luu file.
 * @param path         duong dan TUONG DOI so voi thu muc upload (vd documents/2026/11/<uuid>.pdf) - luu vao cot file_url
 * @param originalName ten file goc nguoi dung tai len (chi de hien thi)
 * @param contentType  MIME da duoc xac thuc theo noi dung file
 * @param size         dung luong (byte)
 */
public record StoredFile(String path, String originalName, String contentType, long size) {
}