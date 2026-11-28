package umastar.service;

import umastar.dto.response.CountryResponse;
import umastar.enums.TransportType;

import java.util.List;

/** Nghiệp vụ danh mục quốc gia (đọc). */
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
}
