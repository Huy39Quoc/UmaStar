package umastar.mapper;

import org.springframework.stereotype.Component;
import umastar.dto.request.CreateCountryRequest;
import umastar.dto.request.UpdateCountryRequest;
import umastar.dto.response.CountryDetailResponse;
import umastar.dto.response.CountryResponse;
import umastar.entity.Country;

/** Chuyển đổi Entity ↔ DTO cho danh mục quốc gia. */
@Component
public class CountryMapper {

    /** Bản rút gọn cho người dùng thường khi chọn điểm đi/đến. */
    public CountryResponse toCountryResponse(Country country) {
        return new CountryResponse(
                country.getId(),
                country.getIsoCode(),
                country.getName(),
                country.getNameVi()
        );
    }

    /** Bản đầy đủ cho màn hình quản trị. */
    public CountryDetailResponse toCountryDetailResponse(Country country) {
        return new CountryDetailResponse(
                country.getId(),
                country.getIsoCode(),
                country.getName(),
                country.getNameVi(),
                country.isHomeCountry(),
                country.isPartner(),
                country.isActive(),
                country.getCreatedAt(),
                country.getUpdatedAt()
        );
    }

    /** Quốc gia mới luôn ở trạng thái hoạt động. */
    public Country toNewCountry(CreateCountryRequest request) {
        Country country = new Country();
        country.setIsoCode(request.isoCode());
        country.setName(request.name());
        country.setNameVi(request.nameVi());
        country.setHomeCountry(request.homeCountry());
        country.setPartner(request.partner());
        country.setActive(true);
        return country;
    }

    public void updateCountry(Country country, UpdateCountryRequest request) {
        country.setIsoCode(request.isoCode());
        country.setName(request.name());
        country.setNameVi(request.nameVi());
        country.setHomeCountry(request.homeCountry());
        country.setPartner(request.partner());
        country.setActive(request.active());
    }
}
