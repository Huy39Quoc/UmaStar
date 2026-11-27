package umastar.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void normalizesEmailAllowsLegacyPasswordAndMasksToString() {
        LoginRequest req = new LoginRequest("  Manager@UmaStar.Local ", "  legacy1  ");

        assertEquals("manager@umastar.local", req.email());
        assertEquals("  legacy1  ", req.password());
        assertFalse(req.toString().contains("legacy1"));
        assertTrue(req.toString().contains("password=***"));
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void rejectsBlankAndOversizedFields() {
        LoginRequest blank = new LoginRequest("   ", "");
        Set<ConstraintViolation<LoginRequest>> blankViolations = validator.validate(blank);
        assertEquals(2, blankViolations.size());

        LoginRequest tooLong = new LoginRequest("a".repeat(151), "p".repeat(129));
        Set<ConstraintViolation<LoginRequest>> lengthViolations = validator.validate(tooLong);
        assertEquals(2, lengthViolations.size());
    }
}

