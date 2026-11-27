package umastar.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import umastar.enums.CustomerType;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void normalizesFieldsAndMasksPasswordInToString() {
        RegisterRequest req = new RegisterRequest(
                "  Nguyễn Văn A  ",
                "  Owner@Club.VN ",
                "  Secret123  ",
                "   ",
                "  CLB Ngựa Đua ",
                CustomerType.CLUB
        );

        assertEquals("Nguyễn Văn A", req.fullName());
        assertEquals("owner@club.vn", req.email());
        assertEquals("  Secret123  ", req.password());
        assertNull(req.phone());
        assertEquals("CLB Ngựa Đua", req.organizationName());
        assertFalse(req.toString().contains("Secret123"));
        assertTrue(req.toString().contains("password=***"));
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void rejectsWeakPasswordAndInvalidFields() {
        RegisterRequest req = new RegisterRequest(
                "   ",
                "khong-hop-le",
                "weakpass",
                "abc-123",
                "",
                null
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertEquals(6, violations.size());
    }

    @Test
    void blankPasswordReportsNotBlankOnlyOnce() {
        RegisterRequest req = new RegisterRequest(
                "Nguyễn Văn A",
                "owner@club.vn",
                "   ",
                "0912345678",
                "CLB Ngựa Đua",
                CustomerType.INDIVIDUAL
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertEquals(1, violations.size());
        assertEquals("Vui lòng nhập mật khẩu", violations.iterator().next().getMessage());
    }
}

