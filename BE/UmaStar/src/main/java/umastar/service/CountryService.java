package umastar.service;

import umastar.dto.request.CreateCountryRequest;
import umastar.dto.request.UpdateCountryRequest;
import umastar.dto.response.CountryDetailResponse;
import umastar.dto.response.CountryResponse;
import umastar.dto.response.PageResponse;
import umastar.enums.TransportType;

import java.util.List;
import java.util.UUID;

/** Nghiệp vụ danh mục quốc gia: đọc cho mọi người dùng và quản trị (CRUD) cho Logistics Manager. */
public interface CountryService {

    /**
     * Danh sách quốc gia hợp lệ để chọn theo loại đơn vận chuyển, chỉ gồm quốc gia đang hoạt động, sắp theo tên:
     * <ul>
     *   <li>{@code DOMESTIC}: chỉ nước sở tại ({@code homeCountry = true}).</li>
     *   <li>{@code INTERNATIONAL}: các nước đã hợp tác ({@code partner = true}) và KHÔNG phải nước sở tại.</li>
     * </ul>
     *
     * @throws umastar.exception.AppException INVALID_REQUEST nếu {@code scope} là null
     */
    List<CountryResponse> listCountries(TransportType scope);

    /** Quản trị: tìm kiếm / phân trang quốc gia (kể cả đã vô hiệu hóa) theo từ khóa (tên, tên tiếng Việt, mã), trạng thái và cờ. */
    PageResponse<CountryDetailResponse> searchCountries(String keyword, Boolean active, Boolean homeCountry,
                                                        Boolean partner, Integer page, Integer size, String sort);

    /** Quản trị: xem chi tiết một quốc gia (kể cả đã vô hiệu hóa). */
    CountryDetailResponse getCountry(UUID countryId);

    /**
     * Quản trị: tạo quốc gia mới.
     *
     * @throws umastar.exception.AppException COUNTRY_CODE_ALREADY_EXISTS nếu mã đã dùng,
     *                                        HOME_COUNTRY_ALREADY_EXISTS nếu đã có nước sở tại khác
     */
    CountryDetailResponse createCountry(CreateCountryRequest request);

    /**
     * Quản trị: cập nhật toàn bộ thông tin quốc gia, gồm cả kích hoạt lại ({@code active = true}).
     *
     * @throws umastar.exception.AppException COUNTRY_NOT_FOUND, COUNTRY_CODE_ALREADY_EXISTS, HOME_COUNTRY_ALREADY_EXISTS
     */
    CountryDetailResponse updateCountry(UUID countryId, UpdateCountryRequest request);

    /** Quản trị: xóa mềm (đặt {@code active = false}); gọi lại trên quốc gia đã vô hiệu hóa vẫn thành công. */
    void deactivateCountry(UUID countryId);
}
