package umastar.service;

import umastar.dto.response.CityResponse;

import java.util.List;
import java.util.UUID;

/** Nghiệp vụ danh mục thành phố (đọc). */
public interface CityService {

    /**
     * Danh sách thành phố đang hoạt động của một quốc gia, sắp theo tên.
     * Quốc gia phải tồn tại và đang hoạt động; quốc gia đã ẩn được coi như không tồn tại.
     *
     * @throws umastar.exception.AppException INVALID_REQUEST nếu {@code countryId} là null;
     *                                        COUNTRY_NOT_FOUND nếu không có quốc gia này hoặc quốc gia đã ngừng hoạt động
     */
    List<CityResponse> listCities(UUID countryId);
}
