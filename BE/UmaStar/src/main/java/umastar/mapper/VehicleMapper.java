package umastar.mapper;

import org.springframework.stereotype.Component;
import umastar.dto.request.CreateVehicleRequest;
import umastar.dto.request.UpdateVehicleRequest;
import umastar.dto.response.VehicleResponse;
import umastar.entity.Vehicle;
import umastar.enums.VehicleStatus;

/** Chuyển đổi Entity ↔ DTO cho phương tiện. */
@Component
public class VehicleMapper {

    public VehicleResponse toVehicleResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getVehicleType(),
                vehicle.getCodeOrPlate(),
                vehicle.getCapacityHorses(),
                vehicle.getCarrierName(),
                vehicle.getStatus(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }

    /** Phương tiện mới luôn ở trạng thái sẵn sàng. */
    public Vehicle toNewVehicle(CreateVehicleRequest request) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleType(request.vehicleType());
        vehicle.setCodeOrPlate(request.codeOrPlate());
        vehicle.setCapacityHorses(request.capacityHorses());
        vehicle.setCarrierName(request.carrierName());
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        return vehicle;
    }

    /** Cập nhật thông tin mô tả. Trạng thái do service kiểm tra quy tắc chuyển rồi mới đặt, không đặt ở đây. */
    public void updateVehicle(Vehicle vehicle, UpdateVehicleRequest request) {
        vehicle.setVehicleType(request.vehicleType());
        vehicle.setCodeOrPlate(request.codeOrPlate());
        vehicle.setCapacityHorses(request.capacityHorses());
        vehicle.setCarrierName(request.carrierName());
    }
}
