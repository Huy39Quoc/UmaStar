package umastar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umastar.entity.Trip;

import java.util.UUID;

/** Mới chỉ có các truy vấn cần cho quản lý phương tiện; sẽ bổ sung khi làm chức năng chuyến vận chuyển. */
public interface TripRepository extends JpaRepository<Trip, UUID> {

    /** Phương tiện đã từng được gán cho bất kỳ chuyến nào chưa (mọi trạng thái, kể cả đã hủy hoặc đã giao). */
    boolean existsByVehicleId(UUID vehicleId);
}
