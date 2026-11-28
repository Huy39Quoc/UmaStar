package umastar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import umastar.dto.response.CityResponse;
import umastar.dto.response.CountryResponse;
import umastar.enums.TransportType;
import umastar.service.CityService;
import umastar.service.CountryService;

import java.util.List;
import java.util.UUID;

/** API danh mục quốc gia và thành phố (mọi người dùng đã đăng nhập đều xem được). */
@RestController
@RequestMapping("/api/v1/countries")
@Tag(name = "Countries", description = "Danh mục quốc gia và thành phố — [Mọi role đã đăng nhập]")
public class CountryController {

    private final CountryService countryService;
    private final CityService cityService;

    public CountryController(CountryService countryService, CityService cityService) {
        this.countryService = countryService;
        this.cityService = cityService;
    }

    /**
     * Danh sách quốc gia theo loại đơn: DOMESTIC chỉ trả nước sở tại; INTERNATIONAL trả các nước đã hợp tác
     * (trừ nước sở tại). Chỉ gồm quốc gia đang hoạt động. Thiếu hoặc sai {@code scope} trả lỗi 400.
     */
    @Operation(summary = "[Mọi role đã đăng nhập] Danh sách quốc gia theo loại đơn",
            description = "Quyền truy cập: Đã đăng nhập (mọi role). Chỉ trả các quốc gia đang hoạt động theo scope DOMESTIC hoặc INTERNATIONAL.")
    @GetMapping
    public List<CountryResponse> listCountries(
            @Parameter(description = "Loại đơn vận chuyển: DOMESTIC (nội địa) hoặc INTERNATIONAL (quốc tế)", required = true)
            @RequestParam TransportType scope) {
        return countryService.listCountries(scope);
    }

    /**
     * Danh sách thành phố đang hoạt động của một quốc gia. Trả 404 nếu quốc gia không tồn tại hoặc đã ngừng hoạt động.
     */
    @Operation(summary = "[Mọi role đã đăng nhập] Danh sách thành phố của một quốc gia",
            description = "Quyền truy cập: Đã đăng nhập (mọi role). Chỉ trả các thành phố đang hoạt động thuộc quốc gia đó.")
    @GetMapping("/{id}/cities")
    public List<CityResponse> listCities(@PathVariable UUID id) {
        return cityService.listCities(id);
    }
}
