package umastar.service;

import umastar.dto.request.CreateCityRequest;
import umastar.dto.request.UpdateCityRequest;
import umastar.dto.response.CityDetailResponse;
import umastar.dto.response.CityResponse;
import umastar.dto.response.PageResponse;

import java.util.List;
import java.util.UUID;

/** Nghiệp vụ danh mục thành phố: đọc cho mọi người dùng và quản trị (CRUD) cho Logistics Manager. */
public interface CityService {

    /**
     * Danh sách thành phố đang hoạt động của một quốc gia, sắp theo tên.
     * Quốc gia phải tồn tại và đang hoạt động; quốc gia đã ẩn được coi như không tồn tại.
     *
     * @throws umastar.exception.AppException COUNTRY_NOT_FOUND nếu không có quốc gia này hoặc quốc gia đã ngừng hoạt động
     */
    List<CityResponse> listCities(UUID countryId);

    /** Quản trị: tìm kiếm / phân trang thành phố (kể cả đã vô hiệu hóa) theo quốc gia, từ khóa (tên, tên tiếng Việt) và trạng thái. */
    PageResponse<CityDetailResponse> searchCities(UUID countryId, String keyword, Boolean active,
                                                  Integer page, Integer size, String sort);

    /** Quản trị: xem chi tiết một thành phố (kể cả đã vô hiệu hóa). */
    CityDetailResponse getCity(UUID cityId);

    /**
     * Quản trị: tạo thành phố mới trong một quốc gia.
     *
     * @throws umastar.exception.AppException COUNTRY_NOT_FOUND, INVALID_TIMEZONE, CITY_NAME_ALREADY_EXISTS
     */
    CityDetailResponse createCity(CreateCityRequest request);

    /**
     * Quản trị: cập nhật toàn bộ thông tin thành phố (không đổi quốc gia), gồm cả kích hoạt lại ({@code active = true}).
     *
     * @throws umastar.exception.AppException CITY_NOT_FOUND, INVALID_TIMEZONE, CITY_NAME_ALREADY_EXISTS
     */
    CityDetailResponse updateCity(UUID cityId, UpdateCityRequest request);

    /** Quản trị: xóa mềm (đặt {@code active = false}); gọi lại trên thành phố đã vô hiệu hóa vẫn thành công. */
    void deactivateCity(UUID cityId);
}
