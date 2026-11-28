package umastar.dto.response;

import java.util.UUID;

/** Thông tin quốc gia trả về cho FE khi chọn điểm đi/đến (không chứa danh sách thành phố hay cờ nội bộ). */
public record CountryResponse(UUID id, String isoCode, String name, String nameVi) {
}
