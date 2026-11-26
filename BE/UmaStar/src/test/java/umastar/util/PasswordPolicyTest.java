package umastar.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyTest {

    @Test
    void acceptsValidPasswords() {
        assertTrue(PasswordPolicy.isValid("ChangeMe@123"));
        assertTrue(PasswordPolicy.isValid("Abcdefg1"));
    }

    @Test
    void rejectsWeakOrOversizedPasswords() {
        assertFalse(PasswordPolicy.isValid(null));
        assertFalse(PasswordPolicy.isValid("Abc1234"));            // < 8 ky tu
        assertFalse(PasswordPolicy.isValid("abcdefgh1"));          // thieu chu hoa
        assertFalse(PasswordPolicy.isValid("ABCDEFGH1"));          // thieu chu thuong
        assertFalse(PasswordPolicy.isValid("Abcdefgh"));           // thieu chu so
        assertFalse(PasswordPolicy.isValid("A1" + "a".repeat(71))); // > 72 byte (BCrypt limit)
    }
}

