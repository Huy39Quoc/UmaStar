package umastar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umastar.entity.City;

import java.util.List;
import java.util.UUID;

public interface CityRepository extends JpaRepository<City, UUID> {

    /** Thành phố đang hoạt động của đúng một quốc gia, sắp theo tên. */
    List<City> findByCountryIdAndActiveTrueOrderByNameAsc(UUID countryId);
}
