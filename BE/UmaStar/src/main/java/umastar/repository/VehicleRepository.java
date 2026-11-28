package umastar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umastar.entity.Vehicle;

import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID>, JpaSpecificationExecutor<Vehicle> {

    /**
     * Biển số / mã phương tiện đã được dùng chưa (không phân biệt hoa thường).
     * Dùng @Query vì tên thuộc tính "codeOrPlate" chứa từ "Or" khiến Spring Data JPA tách nhầm thành (code OR plate).
     */
    @Query("select count(v) > 0 from Vehicle v where lower(v.codeOrPlate) = lower(:codeOrPlate)")
    boolean existsByCodeOrPlateIgnoreCase(@Param("codeOrPlate") String codeOrPlate);

    /** Như trên nhưng bỏ qua chính phương tiện đang sửa. */
    @Query("select count(v) > 0 from Vehicle v where lower(v.codeOrPlate) = lower(:codeOrPlate) and v.id <> :id")
    boolean existsByCodeOrPlateIgnoreCaseAndIdNot(@Param("codeOrPlate") String codeOrPlate, @Param("id") UUID id);
}
