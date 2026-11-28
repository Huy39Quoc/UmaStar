package umastar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import umastar.entity.Country;

import java.util.List;
import java.util.UUID;

public interface CountryRepository extends JpaRepository<Country, UUID>, JpaSpecificationExecutor<Country> {

    /** Nước sở tại đang hoạt động, sắp theo tên (dùng cho đơn NỘI ĐỊA). */
    List<Country> findByHomeCountryTrueAndActiveTrueOrderByNameAsc();

    /** Nước đã hợp tác đang hoạt động, loại trừ nước sở tại, sắp theo tên (dùng cho đơn QUỐC TẾ). */
    List<Country> findByPartnerTrueAndHomeCountryFalseAndActiveTrueOrderByNameAsc();

    /** Mã quốc gia đã được dùng chưa (không phân biệt hoa thường, tính cả quốc gia đã vô hiệu hóa). */
    boolean existsByIsoCodeIgnoreCase(String isoCode);

    /** Như trên nhưng bỏ qua chính quốc gia đang sửa. */
    boolean existsByIsoCodeIgnoreCaseAndIdNot(String isoCode, UUID id);

    /** Đã có quốc gia sở tại nào chưa (tính cả quốc gia đã vô hiệu hóa để tránh có hai nước sở tại khi kích hoạt lại). */
    boolean existsByHomeCountryTrue();

    /** Có quốc gia sở tại nào KHÁC quốc gia đang sửa không. */
    boolean existsByHomeCountryTrueAndIdNot(UUID id);
}
