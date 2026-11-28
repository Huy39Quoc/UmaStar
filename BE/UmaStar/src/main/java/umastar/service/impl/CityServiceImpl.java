package umastar.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umastar.dto.response.CityResponse;
import umastar.entity.Country;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;
import umastar.mapper.CityMapper;
import umastar.repository.CityRepository;
import umastar.repository.CountryRepository;
import umastar.service.CityService;

import java.util.List;
import java.util.UUID;

@Service
public class CityServiceImpl implements CityService {

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
}
