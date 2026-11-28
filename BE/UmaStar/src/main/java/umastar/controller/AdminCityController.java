package umastar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import umastar.dto.request.CreateCityRequest;
import umastar.dto.request.UpdateCityRequest;
import umastar.dto.response.CityDetailResponse;
import umastar.dto.response.PageResponse;
import umastar.service.CityService;

import java.util.UUID;

/** API quản trị danh mục thành phố (chỉ dành cho Logistics Manager). */
@RestController
@RequestMapping("/api/v1/admin/cities")
@PreAuthorize("hasRole('LOGISTICS_MANAGER')")
@Tag(name = "Admin - Cities", description = "Quản trị danh mục thành phố — [Chỉ LOGISTICS_MANAGER]")
public class AdminCityController {

    private final CityService cityService;

    public AdminCityController(CityService cityService) {
        this.cityService = cityService;
    }

    /** Danh sách thành phố (kể cả đã vô hiệu hóa) có lọc theo quốc gia, từ khóa, trạng thái và phân trang. */
    @Operation(summary = "[LOGISTICS_MANAGER] Danh sách thành phố (quản trị)",
            description = "Quyền truy cập: Chỉ LOGISTICS_MANAGER (xem cả thành phố đang hoạt động và đã vô hiệu hóa).")
    @GetMapping
    public PageResponse<CityDetailResponse> searchCities(
            @RequestParam(required = false) UUID countryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return cityService.searchCities(countryId, keyword, active, page, size, sort);
    }

    /** Xem chi tiết một thành phố theo ID (kể cả đã vô hiệu hóa). */
    @Operation(summary = "[LOGISTICS_MANAGER] Chi tiết thành phố (quản trị)",
            description = "Quyền truy cập: Chỉ LOGISTICS_MANAGER.")
    @GetMapping("/{id}")
    public CityDetailResponse getCity(@PathVariable UUID id) {
        return cityService.getCity(id);
    }

    /** Tạo thành phố mới: múi giờ IANA hợp lệ, tên không trùng trong cùng quốc gia. */
    @Operation(summary = "[LOGISTICS_MANAGER] Tạo thành phố",
            description = "Quyền truy cập: Chỉ LOGISTICS_MANAGER.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CityDetailResponse createCity(@Valid @RequestBody CreateCityRequest request) {
        return cityService.createCity(request);
    }

    /** Cập nhật toàn bộ thông tin thành phố (không đổi quốc gia); gửi {@code active = true} để kích hoạt lại. */
    @Operation(summary = "[LOGISTICS_MANAGER] Cập nhật thành phố",
            description = "Quyền truy cập: Chỉ LOGISTICS_MANAGER.")
    @PutMapping("/{id}")
    public CityDetailResponse updateCity(@PathVariable UUID id,
                                         @Valid @RequestBody UpdateCityRequest request) {
        return cityService.updateCity(id, request);
    }

    /** Xóa mềm thành phố: chỉ đặt {@code active = false}, dữ liệu vẫn được giữ lại. */
    @Operation(summary = "[LOGISTICS_MANAGER] Vô hiệu hóa thành phố (xóa mềm)",
            description = "Quyền truy cập: Chỉ LOGISTICS_MANAGER.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateCity(@PathVariable UUID id) {
        cityService.deactivateCity(id);
    }
}
