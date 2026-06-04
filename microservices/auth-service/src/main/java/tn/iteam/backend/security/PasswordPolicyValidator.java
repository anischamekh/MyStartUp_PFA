package tn.iteam.backend.security;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import tn.iteam.backend.exception.BusinessException;

@Component
public class PasswordPolicyValidator {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    private static final Pattern UPPER = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWER = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL = Pattern.compile(".*[^A-Za-z0-9].*");

    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password",
            "password1",
            "password123",
            "12345678",
            "123456789",
            "qwerty123",
            "admin123",
            "changeme",
            "welcome1"
    );

    public void validate(String password) {
        if (password == null) {
            throw new BusinessException("Password is required");
        }
        String normalized = Normalizer.normalize(password, Normalizer.Form.NFKC);
        if (normalized.length() < MIN_LENGTH) {
            throw new BusinessException("Password must be at least " + MIN_LENGTH + " characters");
        }
        if (normalized.length() > MAX_LENGTH) {
            throw new BusinessException("Password must be at most " + MAX_LENGTH + " characters");
        }
        if (COMMON_PASSWORDS.contains(normalized.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("Password is too common; choose a stronger password");
        }
        if (!UPPER.matcher(normalized).matches()) {
            throw new BusinessException("Password must contain an uppercase letter");
        }
        if (!LOWER.matcher(normalized).matches()) {
            throw new BusinessException("Password must contain a lowercase letter");
        }
        if (!DIGIT.matcher(normalized).matches()) {
            throw new BusinessException("Password must contain a number");
        }
        if (!SPECIAL.matcher(normalized).matches()) {
            throw new BusinessException("Password must contain a special character");
        }
    }
}
