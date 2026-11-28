package umastar.mapper;

import org.springframework.stereotype.Component;
import umastar.dto.request.CreateCityRequest;
import umastar.dto.request.UpdateCityRequest;
import umastar.dto.response.CityDetailResponse;
import umastar.dto.response.CityResponse;
import umastar.entity.City;
import umastar.entity.Country;

/** Chuyển đổi Entity ↔ DTO cho danh mục thành phố. */
@Component
public class CityMapper {

    /** Bản rút gọn cho người dùng thường khi chọn điểm đi/đến. */
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

    /** Bản đầy đủ cho màn hình quản trị. */
    public CityDetailResponse toCityDetailResponse(City city) {
        Country country = city.getCountry();
        return new CityDetailResponse(
                city.getId(),
                country.getId(),
                country.getName(),
                city.getName(),
                city.getNameVi(),
                city.getRegion(),
                city.getTimezone(),
                city.isHasAirport(),
                city.isActive(),
                city.getCreatedAt(),
                city.getUpdatedAt()
        );
    }

    /** Thành phố mới luôn ở trạng thái hoạt động. */
    public City toNewCity(CreateCityRequest request, Country country) {
        City city = new City();
        city.setCountry(country);
        city.setName(request.name());
        city.setNameVi(request.nameVi());
        city.setRegion(request.region());
        city.setTimezone(request.timezone());
        city.setHasAirport(request.hasAirport());
        city.setActive(true);
        return city;
    }

    /** Không đổi quốc gia của thành phố. */
    public void updateCity(City city, UpdateCityRequest request) {
        city.setName(request.name());
        city.setNameVi(request.nameVi());
        city.setRegion(request.region());
        city.setTimezone(request.timezone());
        city.setHasAirport(request.hasAirport());
        city.setActive(request.active());
    }
}
