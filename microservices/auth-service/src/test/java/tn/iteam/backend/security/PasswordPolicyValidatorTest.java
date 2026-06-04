package tn.iteam.backend.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tn.iteam.backend.exception.BusinessException;

class PasswordPolicyValidatorTest {

    private final PasswordPolicyValidator validator = new PasswordPolicyValidator();

    @Test
    void acceptsStrongPassword() {
        assertDoesNotThrow(() -> validator.validate("Password1!"));
    }

    @Test
    void rejectsNullOrTooShort() {
        BusinessException ex = assertThrows(BusinessException.class, () -> validator.validate("Ab1!"));
        assertEquals("Password must be at least 8 characters", ex.getMessage());
    }

    @Test
    void rejectsMissingUppercase() {
        BusinessException ex = assertThrows(BusinessException.class, () -> validator.validate("password1!"));
        assertEquals("Password must contain an uppercase letter", ex.getMessage());
    }

    @Test
    void rejectsMissingLowercase() {
        BusinessException ex = assertThrows(BusinessException.class, () -> validator.validate("PASSWORD1!"));
        assertEquals("Password must contain a lowercase letter", ex.getMessage());
    }

    @Test
    void rejectsMissingDigit() {
        BusinessException ex = assertThrows(BusinessException.class, () -> validator.validate("Password!"));
        assertEquals("Password must contain a number", ex.getMessage());
    }

    @Test
    void rejectsMissingSpecial() {
        BusinessException ex = assertThrows(BusinessException.class, () -> validator.validate("SecurePass9"));
        assertEquals("Password must contain a special character", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"weak", "12345678", "NoSpecial1", "noupper1!"})
    void rejectsWeakPasswords(String password) {
        assertThrows(BusinessException.class, () -> validator.validate(password));
    }

    @Test
    void rejectsCommonPassword() {
        BusinessException ex = assertThrows(BusinessException.class, () -> validator.validate("password1"));
        assertTrue(ex.getMessage().contains("common"));
    }

    @Test
    void rejectsTooLongPassword() {
        String longPassword = "Aa1!" + "x".repeat(130);
        BusinessException ex = assertThrows(BusinessException.class, () -> validator.validate(longPassword));
        assertTrue(ex.getMessage().contains("at most"));
    }

    @Test
    void rejectsNullPassword() {
        assertThrows(BusinessException.class, () -> validator.validate(null));
    }
}
