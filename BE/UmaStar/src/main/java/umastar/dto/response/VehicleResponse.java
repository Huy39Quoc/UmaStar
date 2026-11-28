package umastar.dto.response;

import umastar.enums.VehicleStatus;
import umastar.enums.VehicleType;

import java.time.Instant;
import java.util.UUID;

/** Thông tin phương tiện trả về cho màn hình điều phối. */
public record VehicleResponse(
        UUID id,
        VehicleType vehicleType,
        String codeOrPlate,
        int capacityHorses,
        String carrierName,
        VehicleStatus status,
        Instant createdAt,
        Instant updatedAt) {
}
