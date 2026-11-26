package umastar.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;

import java.util.Set;

public final class PageableFactory {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private PageableFactory() {
    }

    public static Pageable of(Integer page, Integer size) {
        return of(page, size, null, Set.of(), Sort.unsorted());
    }

    public static Pageable of(Integer page, Integer size, String sort,
            Set<String> allowedSortFields, Sort defaultSort) {
        int safePage = (page == null || page < 0) ? 0 : page;
        int safeSize = (size == null || size < 1) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        Sort fallback = defaultSort != null ? defaultSort : Sort.unsorted();
        return PageRequest.of(safePage, safeSize, parseSort(sort, allowedSortFields, fallback));
    }

    private static Sort parseSort(String sort, Set<String> allowedSortFields, Sort defaultSort) {
        if (sort == null || sort.isBlank()) {
            return defaultSort;
        }
        String[] parts = sort.split(",");
        if (parts.length > 2) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Tham số sort phải có dạng 'trường,asc' hoặc 'trường,desc'");
        }
        String field = parts[0].trim();
        if (allowedSortFields == null || !allowedSortFields.contains(field)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Trường sắp xếp không hợp lệ: " + field);
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length == 2) {
            String dir = parts[1].trim();
            if (dir.equalsIgnoreCase("desc")) {
                direction = Sort.Direction.DESC;
            } else if (!dir.equalsIgnoreCase("asc")) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Hướng sắp xếp phải là 'asc' hoặc 'desc'");
            }
        }
        return Sort.by(direction, field);
    }
}