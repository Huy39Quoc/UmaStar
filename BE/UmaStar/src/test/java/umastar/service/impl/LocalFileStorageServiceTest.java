package umastar.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;
import umastar.storage.FileCategory;
import umastar.storage.StoredFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalFileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storeLoadAndDeleteValidPdf() {
        LocalFileStorageService service = new LocalFileStorageService(tempDir.toString());
        byte[] pdfBytes = "%PDF-1.4\nhello".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "passport.pdf", "application/pdf", pdfBytes);

        StoredFile stored = service.store(file, FileCategory.DOCUMENT);
        assertTrue(stored.path().startsWith("documents/"));
        assertTrue(stored.path().endsWith(".pdf"));
        assertEquals("application/pdf", stored.contentType());

        Resource loaded = service.load(stored.path());
        assertTrue(loaded.exists());

        service.delete(stored.path());
        assertThrows(AppException.class, () -> service.load(stored.path()));
    }

    @Test
    void rejectSpoofedJpgWithWrongMagicBytes() {
        LocalFileStorageService service = new LocalFileStorageService(tempDir.toString());
        MockMultipartFile fakeJpg = new MockMultipartFile("file", "evil.jpg", "image/jpeg", "not-an-image".getBytes());

        AppException ex = assertThrows(AppException.class, () -> service.store(fakeJpg, FileCategory.IMAGE));
        assertEquals(ErrorCode.UNSUPPORTED_FILE_TYPE, ex.getErrorCode());
    }

    @Test
    void rejectPathTraversalOnLoad() {
        LocalFileStorageService service = new LocalFileStorageService(tempDir.toString());
        AppException ex = assertThrows(AppException.class, () -> service.load("../secret.txt"));
        assertEquals(ErrorCode.INVALID_FILE_NAME, ex.getErrorCode());
    }
}

