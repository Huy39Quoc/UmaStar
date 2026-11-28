package umastar.service.impl;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umastar.dto.request.CreateCityRequest;
import umastar.dto.request.UpdateCityRequest;
import umastar.dto.response.CityDetailResponse;
import umastar.dto.response.CityResponse;
import umastar.dto.response.PageResponse;
import umastar.entity.City;
import umastar.entity.Country;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;
import umastar.mapper.CityMapper;
import umastar.repository.CityRepository;
import umastar.repository.CountryRepository;
import umastar.service.CityService;
import umastar.util.InputNormalizer;
import umastar.util.PageableFactory;
import umastar.util.SqlLike;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CityServiceImpl implements CityService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "createdAt");

    /** Các mã múi giờ IANA hợp lệ (VD: Asia/Tokyo). Không gồm dạng offset như "+07:00" hay "UTC+7". */
    private static final Set<String> VALID_TIME_ZONES = ZoneId.getAvailableZoneIds();

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    public CityServiceImpl(CountryRepository countryRepository,
                           CityRepository cityRepository,
                           CityMapper cityMapper) {
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.cityMapper = cityMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityResponse> listCities(UUID countryId) {
        if (countryId == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        countryRepository.findById(countryId)
                .filter(Country::isActive)
                .orElseThrow(() -> new AppException(ErrorCode.COUNTRY_NOT_FOUND));

        return cityRepository.findByCountryIdAndActiveTrueOrderByNameAsc(countryId).stream()
                .map(cityMapper::toCityResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CityDetailResponse> searchCities(UUID countryId, String keyword, Boolean active,
                                                         Integer page, Integer size, String sort) {
        Pageable pageable = PageableFactory.of(
                page, size, sort, ALLOWED_SORT_FIELDS, Sort.by(Sort.Direction.ASC, "name"));
        Page<City> result = cityRepository.findAll(buildSearchSpec(countryId, keyword, active), pageable);
        return PageResponse.from(result, cityMapper::toCityDetailResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CityDetailResponse getCity(UUID cityId) {
        return cityMapper.toCityDetailResponse(findCityOrThrow(cityId));
    }

    /**
     * Quốc gia phải tồn tại (có thể đang bị vô hiệu hóa để chuẩn bị dữ liệu trước), múi giờ phải hợp lệ, tên không
     * trùng trong cùng quốc gia (không phân biệt hoa thường, tính cả thành phố đã vô hiệu hóa).
     * Trùng tên song song được ràng buộc {@code uk_city_country_name} của DB chặn lại.
     */
    @Override
    @Transactional
    public CityDetailResponse createCity(CreateCityRequest request) {
        Country country = countryRepository.findById(request.countryId())
                .orElseThrow(() -> new AppException(ErrorCode.COUNTRY_NOT_FOUND));
        requireValidTimeZone(request.timezone());
        if (cityRepository.existsByCountryIdAndNameIgnoreCase(country.getId(), request.name())) {
            throw new AppException(ErrorCode.CITY_NAME_ALREADY_EXISTS);
        }
        City saved = cityRepository.saveAndFlush(cityMapper.toNewCity(request, country));
        return cityMapper.toCityDetailResponse(saved);
    }

    @Override
    @Transactional
    public CityDetailResponse updateCity(UUID cityId, UpdateCityRequest request) {
        City city = findCityOrThrow(cityId);
        requireValidTimeZone(request.timezone());
        if (cityRepository.existsByCountryIdAndNameIgnoreCaseAndIdNot(
                city.getCountry().getId(), request.name(), cityId)) {
            throw new AppException(ErrorCode.CITY_NAME_ALREADY_EXISTS);
        }
        cityMapper.updateCity(city, request);
        return cityMapper.toCityDetailResponse(cityRepository.saveAndFlush(city));
    }

    @Override
    @Transactional
    public void deactivateCity(UUID cityId) {
        City city = findCityOrThrow(cityId);
        city.setActive(false);
        cityRepository.save(city);
    }

    private City findCityOrThrow(UUID cityId) {
        return cityRepository.findById(cityId)
                .orElseThrow(() -> new AppException(ErrorCode.CITY_NOT_FOUND));
    }

    private static void requireValidTimeZone(String timezone) {
        if (!VALID_TIME_ZONES.contains(timezone)) {
            throw new AppException(ErrorCode.INVALID_TIMEZONE);
        }
    }

    private static Specification<City> buildSearchSpec(UUID countryId, String keyword, Boolean active) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (countryId != null) {
                predicates.add(cb.equal(root.get("country").get("id"), countryId));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }
            String cleanKeyword = InputNormalizer.blankToNull(keyword);
            if (cleanKeyword != null) {
                String pattern = SqlLike.contains(cleanKeyword);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern, SqlLike.ESCAPE),
                        cb.like(cb.lower(root.get("nameVi")), pattern, SqlLike.ESCAPE)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
