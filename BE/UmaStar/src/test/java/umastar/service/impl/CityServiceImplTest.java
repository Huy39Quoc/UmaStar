package umastar.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umastar.dto.response.CityResponse;
import umastar.entity.City;
import umastar.entity.Country;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;
import umastar.mapper.CityMapper;
import umastar.repository.CityRepository;
import umastar.repository.CountryRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CityServiceImplTest {

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private CityRepository cityRepository;

    @Test
    void listCitiesReturnsActiveCitiesOfActiveCountry() {
        CityServiceImpl service = new CityServiceImpl(countryRepository, cityRepository, new CityMapper());
        UUID countryId = UUID.randomUUID();
        Country vn = new Country();
        vn.setId(countryId);
        vn.setActive(true);

        City hcm = new City();
        UUID hcmId = UUID.randomUUID();
        hcm.setId(hcmId);
        hcm.setCountry(vn);
        hcm.setName("Ho Chi Minh City");
        hcm.setNameVi("TP. Hồ Chí Minh");
        hcm.setRegion("Miền Nam");
        hcm.setTimezone("Asia/Ho_Chi_Minh");
        hcm.setHasAirport(true);
        hcm.setActive(true);

        when(countryRepository.findById(countryId)).thenReturn(Optional.of(vn));
        when(cityRepository.findByCountryIdAndActiveTrueOrderByNameAsc(countryId)).thenReturn(List.of(hcm));

        List<CityResponse> result = service.listCities(countryId);

        assertEquals(1, result.size());
        CityResponse first = result.get(0);
        assertEquals(hcmId, first.id());
        assertEquals("Ho Chi Minh City", first.name());
        assertEquals("TP. Hồ Chí Minh", first.nameVi());
        assertEquals("Miền Nam", first.region());
        assertEquals("Asia/Ho_Chi_Minh", first.timezone());
        assertTrue(first.hasAirport());
        verify(cityRepository).findByCountryIdAndActiveTrueOrderByNameAsc(countryId);
    }

    @Test
    void listCitiesThrowsCountryNotFoundWhenMissing() {
        CityServiceImpl service = new CityServiceImpl(countryRepository, cityRepository, new CityMapper());
        UUID countryId = UUID.randomUUID();
        when(countryRepository.findById(countryId)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> service.listCities(countryId));

        assertEquals(ErrorCode.COUNTRY_NOT_FOUND, ex.getErrorCode());
        verifyNoInteractions(cityRepository);
    }

    @Test
    void listCitiesThrowsCountryNotFoundWhenInactive() {
        CityServiceImpl service = new CityServiceImpl(countryRepository, cityRepository, new CityMapper());
        UUID countryId = UUID.randomUUID();
        Country inactive = new Country();
        inactive.setId(countryId);
        inactive.setActive(false);
        when(countryRepository.findById(countryId)).thenReturn(Optional.of(inactive));

        AppException ex = assertThrows(AppException.class, () -> service.listCities(countryId));

        assertEquals(ErrorCode.COUNTRY_NOT_FOUND, ex.getErrorCode());
        verifyNoInteractions(cityRepository);
    }

    @Test
    void listCitiesNullCountryIdThrowsInvalidRequest() {
        CityServiceImpl service = new CityServiceImpl(countryRepository, cityRepository, new CityMapper());

        AppException ex = assertThrows(AppException.class, () -> service.listCities(null));

        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        verifyNoInteractions(countryRepository, cityRepository);
    }
}

