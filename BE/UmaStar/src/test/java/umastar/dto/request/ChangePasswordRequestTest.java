package umastar.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ChangePasswordRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void preservesPasswordsMasksToStringAndAcceptsValidInput() {
        ChangePasswordRequest req = new ChangePasswordRequest("  OldPass1  ", "  NewPass2  ");

        assertEquals("  OldPass1  ", req.currentPassword());
        assertEquals("  NewPass2  ", req.newPassword());
        assertFalse(req.toString().contains("OldPass1"));
        assertFalse(req.toString().contains("NewPass2"));
        assertTrue(req.toString().contains("currentPassword=***"));
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void rejectsBlankCurrentOrWeakNewPasswordWithoutDuplicateBlankError() {
        Set<ConstraintViolation<ChangePasswordRequest>> weak =
                validator.validate(new ChangePasswordRequest("", "weak"));
        assertEquals(2, weak.size());

        Set<ConstraintViolation<ChangePasswordRequest>> blankNew =
                validator.validate(new ChangePasswordRequest("OldPass1", "   "));
        assertEquals(1, blankNew.size());
        assertEquals("Vui lòng nhập mật khẩu mới", blankNew.iterator().next().getMessage());
    }
}

