package umastar.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import umastar.storage.FileCategory;
import umastar.storage.StoredFile;

public interface FileStorageService {

    StoredFile store(MultipartFile file, FileCategory category);

    Resource load(String path);

    void delete(String path);
}