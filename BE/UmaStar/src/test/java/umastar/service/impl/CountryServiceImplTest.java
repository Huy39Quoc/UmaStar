package umastar.service.impl;

import org.junit.jupiter.api.Test;
import umastar.dto.request.CreateCountryRequest;
import umastar.dto.request.UpdateCountryRequest;
import umastar.dto.response.CountryDetailResponse;
import umastar.dto.response.CountryResponse;
import umastar.entity.Country;
import umastar.enums.TransportType;
import umastar.exception.AppException;
import umastar.exception.ErrorCode;
import umastar.mapper.CountryMapper;
import umastar.repository.CountryRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CountryServiceImplTest {

    private final CountryRepository countryRepository = mock(CountryRepository.class);
    private final CountryMapper countryMapper = new CountryMapper();
    private final CountryServiceImpl countryService = new CountryServiceImpl(countryRepository, countryMapper);

    @Test
    void listCountriesDomesticReturnsActiveHomeCountriesOnly() {
        Country vn = country(UUID.randomUUID(), "VN", "Vietnam", "Việt Nam", true, false, true);
        when(countryRepository.findByHomeCountryTrueAndActiveTrueOrderByNameAsc()).thenReturn(List.of(vn));

        List<CountryResponse> result = countryService.listCountries(TransportType.DOMESTIC);

        assertEquals(1, result.size());
        assertEquals("VN", result.get(0).isoCode());
        assertEquals("Việt Nam", result.get(0).nameVi());
        verify(countryRepository).findByHomeCountryTrueAndActiveTrueOrderByNameAsc();
        verifyNoMoreInteractions(countryRepository);
    }

    @Test
    void listCountriesInternationalReturnsActivePartnerNonHomeCountries() {
        Country jp = country(UUID.randomUUID(), "JP", "Japan", "Nhật Bản", false, true, true);
        Country sg = country(UUID.randomUUID(), "SG", "Singapore", "Singapore", false, true, true);
        when(countryRepository.findByPartnerTrueAndHomeCountryFalseAndActiveTrueOrderByNameAsc())
                .thenReturn(List.of(jp, sg));

        List<CountryResponse> result = countryService.listCountries(TransportType.INTERNATIONAL);

        assertEquals(2, result.size());
        assertEquals("JP", result.get(0).isoCode());
        assertEquals("SG", result.get(1).isoCode());
        verify(countryRepository).findByPartnerTrueAndHomeCountryFalseAndActiveTrueOrderByNameAsc();
        verifyNoMoreInteractions(countryRepository);
    }

    @Test
    void listCountriesNullScopeThrowsInvalidRequest() {
        AppException ex = assertThrows(AppException.class, () -> countryService.listCountries(null));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        verifyNoInteractions(countryRepository);
    }

    @Test
    void createCountryValidatesUniqueCodeAndSingleHomeCountry() {
        when(countryRepository.existsByIsoCodeIgnoreCase("VN")).thenReturn(false);
        when(countryRepository.existsByHomeCountryTrue()).thenReturn(true);

        AppException homeEx = assertThrows(AppException.class,
                () -> countryService.createCountry(new CreateCountryRequest("vn", "Vietnam", "Việt Nam", true, false)));
        assertEquals(ErrorCode.HOME_COUNTRY_ALREADY_EXISTS, homeEx.getErrorCode());

        when(countryRepository.existsByIsoCodeIgnoreCase("JP")).thenReturn(true);
        AppException dupEx = assertThrows(AppException.class,
                () -> countryService.createCountry(new CreateCountryRequest("jp", "Japan", "Nhật Bản", false, true)));
        assertEquals(ErrorCode.COUNTRY_CODE_ALREADY_EXISTS, dupEx.getErrorCode());

        when(countryRepository.existsByIsoCodeIgnoreCase("SG")).thenReturn(false);
        when(countryRepository.saveAndFlush(any(Country.class))).thenAnswer(inv -> {
            Country c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CountryDetailResponse created = countryService.createCountry(
                new CreateCountryRequest(" sg ", "Singapore", "  ", false, true));
        assertEquals("SG", created.isoCode());
        assertNull(created.nameVi());
        assertTrue(created.active());
    }

    @Test
    void updateAndDeactivateCountryWorkAndCheckConstraints() {
        UUID id = UUID.randomUUID();
        Country existing = country(id, "JP", "Japan", "Nhật Bản", false, true, false);
        when(countryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(countryRepository.existsByIsoCodeIgnoreCaseAndIdNot("JP", id)).thenReturn(false);
        when(countryRepository.saveAndFlush(any(Country.class))).thenAnswer(inv -> inv.getArgument(0));

        CountryDetailResponse reactivated = countryService.updateCountry(id,
                new UpdateCountryRequest("jp", "Japan Updated", "Nhật Bản", false, true, true));
        assertTrue(reactivated.active());
        assertEquals("Japan Updated", reactivated.name());

        countryService.deactivateCountry(id);
        assertFalse(existing.isActive());
        verify(countryRepository).save(existing);
    }

    private static Country country(UUID id, String isoCode, String name, String nameVi,
                                   boolean homeCountry, boolean partner, boolean active) {
        Country c = new Country();
        c.setId(id);
        c.setIsoCode(isoCode);
        c.setName(name);
        c.setNameVi(nameVi);
        c.setHomeCountry(homeCountry);
        c.setPartner(partner);
        c.setActive(active);
        return c;
    }
}

