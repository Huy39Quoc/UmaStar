package umastar.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void trimsTokenAndMasksToString() {
        RefreshTokenRequest req = new RefreshTokenRequest("  raw-token-abc  ");

        assertEquals("raw-token-abc", req.refreshToken());
        assertFalse(req.toString().contains("raw-token-abc"));
        assertTrue(req.toString().contains("refreshToken=***"));
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void rejectsBlankAndOversizedToken() {
        Set<ConstraintViolation<RefreshTokenRequest>> blank = validator.validate(new RefreshTokenRequest("   "));
        assertEquals(1, blank.size());

        Set<ConstraintViolation<RefreshTokenRequest>> tooLong = validator.validate(new RefreshTokenRequest("a".repeat(513)));
        assertEquals(1, tooLong.size());
    }
}

