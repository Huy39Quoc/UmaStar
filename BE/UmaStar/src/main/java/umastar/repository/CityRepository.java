package umastar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import umastar.entity.City;

import java.util.List;
import java.util.UUID;

public interface CityRepository extends JpaRepository<City, UUID>, JpaSpecificationExecutor<City> {

    /** Thành phố đang hoạt động của đúng một quốc gia, sắp theo tên. */
    List<City> findByCountryIdAndActiveTrueOrderByNameAsc(UUID countryId);

    /** Tên thành phố đã có trong quốc gia này chưa (không phân biệt hoa thường, tính cả thành phố đã vô hiệu hóa). */
    boolean existsByCountryIdAndNameIgnoreCase(UUID countryId, String name);

    /** Như trên nhưng bỏ qua chính thành phố đang sửa. */
    boolean existsByCountryIdAndNameIgnoreCaseAndIdNot(UUID countryId, String name, UUID id);

    /** Tìm kiếm có phân trang cho màn hình quản trị; nạp sẵn quốc gia để không phát sinh N+1 khi trả tên quốc gia. */
    @Override
    @EntityGraph(attributePaths = "country")
    Page<City> findAll(Specification<City> spec, Pageable pageable);
}
