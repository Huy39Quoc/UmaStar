package umastar.mapper;

import org.springframework.stereotype.Component;
import umastar.dto.response.CountryResponse;
import umastar.entity.Country;

/** Chuyển đổi Entity ↔ DTO cho danh mục quốc gia. */
@Component
public class CountryMapper {

    public CountryResponse toCountryResponse(Country country) {
        return new CountryResponse(
                country.getId(),
                country.getIsoCode(),
                country.getName(),
                country.getNameVi()
        );
    }
}
