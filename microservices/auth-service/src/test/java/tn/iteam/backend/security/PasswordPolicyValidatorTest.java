package tn.iteam.backend.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import tn.iteam.backend.exception.BusinessException;

class PasswordPolicyValidatorTest {

    private final PasswordPolicyValidator validator = new PasswordPolicyValidator();

    @Test
    void acceptsStrongPassword() {
        assertDoesNotThrow(() -> validator.validate("Password1!"));
    }

    @Test
    void rejectsWeakPassword() {
        assertThrows(BusinessException.class, () -> validator.validate("weak"));
    }
}
