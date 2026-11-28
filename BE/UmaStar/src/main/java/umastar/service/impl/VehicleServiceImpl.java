package umastar.service.impl;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umastar.dto.request.CreateVehicleRequest;
import umastar.dto.request.UpdateVehicleRequest;
import umastar.dto.response.PageResponse;
import umastar.dto.response.VehicleResponse;
import umastar.entity.Vehicle;
import umastar.enums.VehicleStatus;
import umastar.enums.VehicleType;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;
import umastar.mapper.VehicleMapper;
import umastar.repository.TripRepository;
import umastar.repository.VehicleRepository;
import umastar.service.VehicleService;
import umastar.util.InputNormalizer;
import umastar.util.PageableFactory;
import umastar.util.SqlLike;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class VehicleServiceImpl implements VehicleService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("codeOrPlate", "capacityHorses", "createdAt");

    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final VehicleMapper vehicleMapper;

    public VehicleServiceImpl(VehicleRepository vehicleRepository,
                              TripRepository tripRepository,
                              VehicleMapper vehicleMapper) {
        this.vehicleRepository = vehicleRepository;
        this.tripRepository = tripRepository;
        this.vehicleMapper = vehicleMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VehicleResponse> searchVehicles(String keyword, VehicleType vehicleType, VehicleStatus status,
                                                        Integer page, Integer size, String sort) {
        Pageable pageable = PageableFactory.of(
                page, size, sort, ALLOWED_SORT_FIELDS, Sort.by(Sort.Direction.ASC, "codeOrPlate"));
        Page<Vehicle> result = vehicleRepository.findAll(buildSearchSpec(keyword, vehicleType, status), pageable);
        return PageResponse.from(result, vehicleMapper::toVehicleResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicle(UUID vehicleId) {
        return vehicleMapper.toVehicleResponse(findVehicleOrThrow(vehicleId));
    }

    /**
     * Hai yêu cầu tạo cùng biển số chạy song song: ràng buộc {@code uk_vehicles_code_or_plate} của DB sẽ chặn và
     * {@code GlobalExceptionHandler} đổi thành VEHICLE_CODE_ALREADY_EXISTS.
     */
    @Override
    @Transactional
    public VehicleResponse createVehicle(CreateVehicleRequest request) {
        if (vehicleRepository.existsByCodeOrPlateIgnoreCase(request.codeOrPlate())) {
            throw new AppException(ErrorCode.VEHICLE_CODE_ALREADY_EXISTS);
        }
        Vehicle saved = vehicleRepository.saveAndFlush(vehicleMapper.toNewVehicle(request));
        return vehicleMapper.toVehicleResponse(saved);
    }

    /** Mọi kiểm tra chạy TRƯỚC khi gán dữ liệu, nên khi báo lỗi thì phương tiện không bị đổi dở. */
    @Override
    @Transactional
    public VehicleResponse updateVehicle(UUID vehicleId, UpdateVehicleRequest request) {
        Vehicle vehicle = findVehicleOrThrow(vehicleId);
        if (vehicleRepository.existsByCodeOrPlateIgnoreCaseAndIdNot(request.codeOrPlate(), vehicleId)) {
            throw new AppException(ErrorCode.VEHICLE_CODE_ALREADY_EXISTS);
        }
        requireAllowedStatusChange(vehicle.getStatus(), request.status());

        vehicleMapper.updateVehicle(vehicle, request);
        vehicle.setStatus(request.status());
        return vehicleMapper.toVehicleResponse(vehicleRepository.saveAndFlush(vehicle));
    }

    @Override
    @Transactional
    public void deleteVehicle(UUID vehicleId) {
        Vehicle vehicle = findVehicleOrThrow(vehicleId);
        if (vehicle.getStatus() == VehicleStatus.IN_USE || tripRepository.existsByVehicleId(vehicleId)) {
            throw new AppException(ErrorCode.VEHICLE_IN_USE);
        }
        vehicleRepository.delete(vehicle);
        vehicleRepository.flush();
    }

    /**
     * Chỉ cho đổi thủ công giữa AVAILABLE và MAINTENANCE.
     * <ul>
     *   <li>Giữ nguyên trạng thái: luôn hợp lệ (để sửa thông tin phương tiện đang IN_USE).</li>
     *   <li>Đặt thành IN_USE: không được, IN_USE do hệ thống gán khi phương tiện vào chuyến.</li>
     *   <li>Đang IN_USE mà chuyển MAINTENANCE: VEHICLE_IN_USE (quy tắc roadmap).</li>
     *   <li>Đang IN_USE mà chuyển AVAILABLE: không được, phải hoàn tất hoặc hủy chuyến để giải phóng.</li>
     * </ul>
     */
    private static void requireAllowedStatusChange(VehicleStatus current, VehicleStatus requested) {
        if (requested == current) {
            return;
        }
        if (requested == VehicleStatus.IN_USE) {
            throw new AppException(ErrorCode.INVALID_STATE_TRANSITION,
                    "Trạng thái IN_USE do hệ thống tự cập nhật khi phương tiện được gán vào chuyến, không đặt thủ công");
        }
        if (current == VehicleStatus.IN_USE) {
            if (requested == VehicleStatus.MAINTENANCE) {
                throw new AppException(ErrorCode.VEHICLE_IN_USE);
            }
            throw new AppException(ErrorCode.INVALID_STATE_TRANSITION,
                    "Phương tiện đang được sử dụng trong chuyến; hãy hoàn tất hoặc hủy chuyến để giải phóng");
        }
    }

    private Vehicle findVehicleOrThrow(UUID vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));
    }

    private static Specification<Vehicle> buildSearchSpec(String keyword, VehicleType vehicleType, VehicleStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (vehicleType != null) {
                predicates.add(cb.equal(root.get("vehicleType"), vehicleType));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            String cleanKeyword = InputNormalizer.blankToNull(keyword);
            if (cleanKeyword != null) {
                String pattern = SqlLike.contains(cleanKeyword);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("codeOrPlate")), pattern, SqlLike.ESCAPE),
                        cb.like(cb.lower(root.get("carrierName")), pattern, SqlLike.ESCAPE)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
