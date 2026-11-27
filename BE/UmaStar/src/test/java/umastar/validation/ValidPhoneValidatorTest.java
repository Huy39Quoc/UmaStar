package umastar.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidPhoneValidatorTest {

    private final ValidPhoneValidator validator = new ValidPhoneValidator();

    @Test
    void acceptsNullBlankAndValidVietnamesePhoneNumbers() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("   ", null));
        assertTrue(validator.isValid("0912345678", null));
        assertTrue(validator.isValid("0323456789", null));
        assertTrue(validator.isValid("0561234567", null));
        assertTrue(validator.isValid("0701234567", null));
        assertTrue(validator.isValid("0861234567", null));
        assertTrue(validator.isValid("+84912345678", null));
        assertTrue(validator.isValid("84912345678", null));
    }

    @Test
    void rejectsInvalidPrefixLengthOrForeignNumbers() {
        assertFalse(validator.isValid("0123456789", null));   // dau 01 cu khong hop le
        assertFalse(validator.isValid("091234567", null));    // thieu so (9 chu so)
        assertFalse(validator.isValid("09123456789", null));  // thua so (11 chu so)
        assertFalse(validator.isValid("+12025550123", null)); // ma quoc gia khac
        assertFalse(validator.isValid("0912 345 678", null)); // chua khoang trang
        assertFalse(validator.isValid("abc", null));
    }
}

