package umastar.dto.response;

import java.time.Instant;
import java.util.UUID;

/** Thông tin đầy đủ của thành phố dành cho màn hình quản trị (gồm quốc gia và trạng thái hoạt động). */
public record CityDetailResponse(
        UUID id,
        UUID countryId,
        String countryName,
        String name,
        String nameVi,
        String region,
        String timezone,
        boolean hasAirport,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}
