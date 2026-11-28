package umastar.mapper;

import org.springframework.stereotype.Component;
import umastar.dto.response.CityResponse;
import umastar.entity.City;

/** Chuyển đổi Entity ↔ DTO cho danh mục thành phố. */
@Component
public class CityMapper {

    public CityResponse toCityResponse(City city) {
        return new CityResponse(
                city.getId(),
                city.getName(),
                city.getNameVi(),
                city.getRegion(),
                city.getTimezone(),
                city.isHasAirport()
        );
    }
}
