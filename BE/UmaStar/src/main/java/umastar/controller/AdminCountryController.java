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
import umastar.dto.request.CreateCountryRequest;
import umastar.dto.request.UpdateCountryRequest;
import umastar.dto.response.CountryDetailResponse;
import umastar.dto.response.PageResponse;
import umastar.service.CountryService;

import java.util.UUID;

/** API quản trị danh mục quốc gia (chỉ dành cho Logistics Manager). */
@RestController
@RequestMapping("/api/v1/admin/countries")
@PreAuthorize("hasRole('LOGISTICS_MANAGER')")
@Tag(name = "Admin - Countries", description = "Quản trị danh mục quốc gia — [Chỉ LOGISTICS_MANAGER]")
public class AdminCountryController {

    private final CountryService countryService;

    public AdminCountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    /** Danh sách quốc gia (kể cả đã vô hiệu hóa) có lọc theo từ khóa, trạng thái, cờ nước sở tại/hợp tác và phân trang. */
    @Operation(summary = "[LOGISTICS_MANAGER] Danh sách quốc gia (quản trị)",
            description = "Quyền truy cập: Chỉ LOGISTICS_MANAGER (xem cả quốc gia đang hoạt động và đã vô hiệu hóa).")
    @GetMapping
    public PageResponse<CountryDetailResponse> searchCountries(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean homeCountry,
            @RequestParam(required = false) Boolean partner,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return countryService.searchCountries(keyword, active, homeCountry, partner, page, size, sort);
    }

    /** Xem chi tiết một quốc gia theo ID (kể cả đã vô hiệu hóa). */
    @Operation(summary = "[LOGISTICS_MANAGER] Chi tiết quốc gia (quản trị)",
            description = "Quyền truy cập: Chỉ LOGISTICS_MANAGER.")
    @GetMapping("/{id}")
    public CountryDetailResponse getCountry(@PathVariable UUID id) {
        return countryService.getCountry(id);
    }

    /** Tạo quốc gia mới: mã ISO 2 chữ in hoa không trùng, tối đa một nước sở tại. */
    @Operation(summary = "[LOGISTICS_MANAGER] Tạo quốc gia",
            description = "Quyền truy cập: Chỉ LOGISTICS_MANAGER.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CountryDetailResponse createCountry(@Valid @RequestBody CreateCountryRequest request) {
        return countryService.createCountry(request);
    }

    /** Cập nhật toàn bộ thông tin quốc gia; gửi {@code active = true} để kích hoạt lại quốc gia đã vô hiệu hóa. */
    @Operation(summary = "[LOGISTICS_MANAGER] Cập nhật quốc gia",
            description = "Quyền truy cập: Chỉ LOGISTICS_MANAGER.")
    @PutMapping("/{id}")
    public CountryDetailResponse updateCountry(@PathVariable UUID id,
                                               @Valid @RequestBody UpdateCountryRequest request) {
        return countryService.updateCountry(id, request);
    }

    /** Xóa mềm quốc gia: chỉ đặt {@code active = false}, dữ liệu vẫn được giữ lại. */
    @Operation(summary = "[LOGISTICS_MANAGER] Vô hiệu hóa quốc gia (xóa mềm)",
            description = "Quyền truy cập: Chỉ LOGISTICS_MANAGER.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateCountry(@PathVariable UUID id) {
        countryService.deactivateCountry(id);
    }
}
