package umastar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umastar.entity.Country;

import java.util.List;
import java.util.UUID;

public interface CountryRepository extends JpaRepository<Country, UUID> {

    /** Nước sở tại đang hoạt động, sắp theo tên (dùng cho đơn NỘI ĐỊA). */
    List<Country> findByHomeCountryTrueAndActiveTrueOrderByNameAsc();

    /** Nước đã hợp tác đang hoạt động, loại trừ nước sở tại, sắp theo tên (dùng cho đơn QUỐC TẾ). */
    List<Country> findByPartnerTrueAndHomeCountryFalseAndActiveTrueOrderByNameAsc();
}
