package umastar.service.impl;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umastar.dto.request.CreateCountryRequest;
import umastar.dto.request.UpdateCountryRequest;
import umastar.dto.response.CountryDetailResponse;
import umastar.dto.response.CountryResponse;
import umastar.dto.response.PageResponse;
import umastar.entity.Country;
import umastar.enums.TransportType;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;
import umastar.mapper.CountryMapper;
import umastar.repository.CountryRepository;
import umastar.service.CountryService;
import umastar.util.InputNormalizer;
import umastar.util.PageableFactory;
import umastar.util.SqlLike;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CountryServiceImpl implements CountryService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "isoCode", "createdAt");

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    public CountryServiceImpl(CountryRepository countryRepository, CountryMapper countryMapper) {
        this.countryRepository = countryRepository;
        this.countryMapper = countryMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CountryResponse> listCountries(TransportType scope) {
        if (scope == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        List<Country> countries = switch (scope) {
            case DOMESTIC -> countryRepository.findByHomeCountryTrueAndActiveTrueOrderByNameAsc();
            case INTERNATIONAL -> countryRepository.findByPartnerTrueAndHomeCountryFalseAndActiveTrueOrderByNameAsc();
        };
        return countries.stream().map(countryMapper::toCountryResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CountryDetailResponse> searchCountries(String keyword, Boolean active, Boolean homeCountry,
                                                               Boolean partner, Integer page, Integer size, String sort) {
        Pageable pageable = PageableFactory.of(
                page, size, sort, ALLOWED_SORT_FIELDS, Sort.by(Sort.Direction.ASC, "name"));
        Page<Country> result = countryRepository.findAll(
                buildSearchSpec(keyword, active, homeCountry, partner), pageable);
        return PageResponse.from(result, countryMapper::toCountryDetailResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CountryDetailResponse getCountry(UUID countryId) {
        return countryMapper.toCountryDetailResponse(findCountryOrThrow(countryId));
    }

    /**
     * Mã quốc gia phải chưa dùng; chỉ được có MỘT nước sở tại trong toàn hệ thống (tính cả nước đã vô hiệu hóa).
     * Hai yêu cầu tạo cùng mã chạy song song: ràng buộc {@code uk_countries_iso_code} của DB sẽ chặn và
     * {@code GlobalExceptionHandler} đổi thành COUNTRY_CODE_ALREADY_EXISTS.
     */
    @Override
    @Transactional
    public CountryDetailResponse createCountry(CreateCountryRequest request) {
        if (countryRepository.existsByIsoCodeIgnoreCase(request.isoCode())) {
            throw new AppException(ErrorCode.COUNTRY_CODE_ALREADY_EXISTS);
        }
        if (request.homeCountry() && countryRepository.existsByHomeCountryTrue()) {
            throw new AppException(ErrorCode.HOME_COUNTRY_ALREADY_EXISTS);
        }
        Country saved = countryRepository.saveAndFlush(countryMapper.toNewCountry(request));
        return countryMapper.toCountryDetailResponse(saved);
    }

    @Override
    @Transactional
    public CountryDetailResponse updateCountry(UUID countryId, UpdateCountryRequest request) {
        Country country = findCountryOrThrow(countryId);
        if (countryRepository.existsByIsoCodeIgnoreCaseAndIdNot(request.isoCode(), countryId)) {
            throw new AppException(ErrorCode.COUNTRY_CODE_ALREADY_EXISTS);
        }
        if (request.homeCountry() && countryRepository.existsByHomeCountryTrueAndIdNot(countryId)) {
            throw new AppException(ErrorCode.HOME_COUNTRY_ALREADY_EXISTS);
        }
        countryMapper.updateCountry(country, request);
        return countryMapper.toCountryDetailResponse(countryRepository.saveAndFlush(country));
    }

    @Override
    @Transactional
    public void deactivateCountry(UUID countryId) {
        Country country = findCountryOrThrow(countryId);
        country.setActive(false);
        countryRepository.save(country);
    }

    private Country findCountryOrThrow(UUID countryId) {
        return countryRepository.findById(countryId)
                .orElseThrow(() -> new AppException(ErrorCode.COUNTRY_NOT_FOUND));
    }

    private static Specification<Country> buildSearchSpec(String keyword, Boolean active,
                                                          Boolean homeCountry, Boolean partner) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }
            if (homeCountry != null) {
                predicates.add(cb.equal(root.get("homeCountry"), homeCountry));
            }
            if (partner != null) {
                predicates.add(cb.equal(root.get("partner"), partner));
            }
            String cleanKeyword = InputNormalizer.blankToNull(keyword);
            if (cleanKeyword != null) {
                String pattern = SqlLike.contains(cleanKeyword);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern, SqlLike.ESCAPE),
                        cb.like(cb.lower(root.get("nameVi")), pattern, SqlLike.ESCAPE),
                        cb.like(cb.lower(root.get("isoCode")), pattern, SqlLike.ESCAPE)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
