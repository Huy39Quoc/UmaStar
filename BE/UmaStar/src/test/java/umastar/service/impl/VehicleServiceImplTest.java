package umastar.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umastar.dto.request.CreateVehicleRequest;
import umastar.dto.request.UpdateVehicleRequest;
import umastar.dto.response.VehicleResponse;
import umastar.entity.Vehicle;
import umastar.enums.VehicleStatus;
import umastar.enums.VehicleType;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;
import umastar.mapper.VehicleMapper;
import umastar.repository.TripRepository;
import umastar.repository.VehicleRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private TripRepository tripRepository;

    private VehicleServiceImpl service() {
        return new VehicleServiceImpl(vehicleRepository, tripRepository, new VehicleMapper());
    }

    @Test
    void createVehicleStartsAvailableAndNormalizesPlate() {
        CreateVehicleRequest req = new CreateVehicleRequest(VehicleType.HORSE_TRUCK, " 51a-12345 ", 4, " Uma Carrier ");
        when(vehicleRepository.existsByCodeOrPlateIgnoreCase("51A-12345")).thenReturn(false);
        when(vehicleRepository.saveAndFlush(any(Vehicle.class))).thenAnswer(inv -> {
            Vehicle v = inv.getArgument(0);
            v.setId(UUID.randomUUID());
            return v;
        });

        VehicleResponse res = service().createVehicle(req);

        assertEquals("51A-12345", res.codeOrPlate());
        assertEquals(VehicleStatus.AVAILABLE, res.status());
        assertEquals("Uma Carrier", res.carrierName());
    }

    @Test
    void createVehicleRejectsDuplicatePlate() {
        CreateVehicleRequest req = new CreateVehicleRequest(VehicleType.AIR_STALL, "STALL-01", 2, null);
        when(vehicleRepository.existsByCodeOrPlateIgnoreCase("STALL-01")).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> service().createVehicle(req));

        assertEquals(ErrorCode.VEHICLE_CODE_ALREADY_EXISTS, ex.getErrorCode());
        verify(vehicleRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateVehicleAllowsAvailableToMaintenanceAndRejectsInvalidTransitions() {
        UUID id = UUID.randomUUID();
        Vehicle available = vehicle(id, VehicleStatus.AVAILABLE);
        when(vehicleRepository.findById(id)).thenReturn(Optional.of(available));
        when(vehicleRepository.existsByCodeOrPlateIgnoreCaseAndIdNot("51A-99999", id)).thenReturn(false);
        when(vehicleRepository.saveAndFlush(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        VehicleResponse updated = service().updateVehicle(id,
                new UpdateVehicleRequest(VehicleType.HORSE_TRUCK, "51A-99999", 6, "Carrier X", VehicleStatus.MAINTENANCE));
        assertEquals(VehicleStatus.MAINTENANCE, updated.status());
        assertEquals(6, updated.capacityHorses());

        Vehicle inUse = vehicle(id, VehicleStatus.IN_USE);
        when(vehicleRepository.findById(id)).thenReturn(Optional.of(inUse));

        AppException toMaintenance = assertThrows(AppException.class, () -> service().updateVehicle(id,
                new UpdateVehicleRequest(VehicleType.HORSE_TRUCK, "51A-99999", 4, null, VehicleStatus.MAINTENANCE)));
        assertEquals(ErrorCode.VEHICLE_IN_USE, toMaintenance.getErrorCode());

        AppException toAvailable = assertThrows(AppException.class, () -> service().updateVehicle(id,
                new UpdateVehicleRequest(VehicleType.HORSE_TRUCK, "51A-99999", 4, null, VehicleStatus.AVAILABLE)));
        assertEquals(ErrorCode.INVALID_STATE_TRANSITION, toAvailable.getErrorCode());

        Vehicle availableAgain = vehicle(id, VehicleStatus.AVAILABLE);
        when(vehicleRepository.findById(id)).thenReturn(Optional.of(availableAgain));
        AppException manualInUse = assertThrows(AppException.class, () -> service().updateVehicle(id,
                new UpdateVehicleRequest(VehicleType.HORSE_TRUCK, "51A-99999", 4, null, VehicleStatus.IN_USE)));
        assertEquals(ErrorCode.INVALID_STATE_TRANSITION, manualInUse.getErrorCode());
    }

    @Test
    void deleteVehicleRejectsWhenInUseOrUsedInTripAndDeletesWhenUnused() {
        UUID inUseId = UUID.randomUUID();
        when(vehicleRepository.findById(inUseId)).thenReturn(Optional.of(vehicle(inUseId, VehicleStatus.IN_USE)));
        AppException ex1 = assertThrows(AppException.class, () -> service().deleteVehicle(inUseId));
        assertEquals(ErrorCode.VEHICLE_IN_USE, ex1.getErrorCode());

        UUID usedInTripId = UUID.randomUUID();
        when(vehicleRepository.findById(usedInTripId)).thenReturn(Optional.of(vehicle(usedInTripId, VehicleStatus.AVAILABLE)));
        when(tripRepository.existsByVehicleId(usedInTripId)).thenReturn(true);
        AppException ex2 = assertThrows(AppException.class, () -> service().deleteVehicle(usedInTripId));
        assertEquals(ErrorCode.VEHICLE_IN_USE, ex2.getErrorCode());

        UUID cleanId = UUID.randomUUID();
        Vehicle clean = vehicle(cleanId, VehicleStatus.MAINTENANCE);
        when(vehicleRepository.findById(cleanId)).thenReturn(Optional.of(clean));
        when(tripRepository.existsByVehicleId(cleanId)).thenReturn(false);

        service().deleteVehicle(cleanId);

        verify(vehicleRepository).delete(clean);
        verify(vehicleRepository).flush();
    }

    private static Vehicle vehicle(UUID id, VehicleStatus status) {
        Vehicle v = new Vehicle();
        v.setId(id);
        v.setVehicleType(VehicleType.HORSE_TRUCK);
        v.setCodeOrPlate("51A-00001");
        v.setCapacityHorses(4);
        v.setStatus(status);
        return v;
    }
}

