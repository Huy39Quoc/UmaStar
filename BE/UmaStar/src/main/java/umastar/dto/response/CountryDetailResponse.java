package umastar.dto.response;

import java.time.Instant;
import java.util.UUID;

/** Thông tin đầy đủ của quốc gia dành cho màn hình quản trị (gồm cờ nội bộ và trạng thái hoạt động). */
public record CountryDetailResponse(
        UUID id,
        String isoCode,
        String name,
        String nameVi,
        boolean homeCountry,
        boolean partner,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}
