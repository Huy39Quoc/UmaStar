package umastar.dto.response;

import java.util.UUID;

/**
 * Thông tin thành phố trả về cho FE khi chọn điểm đi/đến.
 * {@code timezone} là múi giờ IANA (VD: Asia/Ho_Chi_Minh) để hiển thị giờ địa phương.
 */
public record CityResponse(
        UUID id,
        String name,
        String nameVi,
        String region,
        String timezone,
        boolean hasAirport) {
}
