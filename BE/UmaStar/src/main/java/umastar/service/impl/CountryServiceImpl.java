package umastar.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umastar.dto.response.CountryResponse;
import umastar.entity.Country;
import umastar.enums.TransportType;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;
import umastar.mapper.CountryMapper;
import umastar.repository.CountryRepository;
import umastar.service.CountryService;

import java.util.List;

@Service
public class CountryServiceImpl implements CountryService {

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
}
