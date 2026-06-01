package tn.iteam.backend.security;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import tn.iteam.backend.exception.BusinessException;

@Component
public class PasswordPolicyValidator {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPER = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWER = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL = Pattern.compile(".*[^A-Za-z0-9].*");

    public void validate(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new BusinessException("Password must be at least " + MIN_LENGTH + " characters");
        }
        if (!UPPER.matcher(password).matches()) {
            throw new BusinessException("Password must contain an uppercase letter");
        }
        if (!LOWER.matcher(password).matches()) {
            throw new BusinessException("Password must contain a lowercase letter");
        }
        if (!DIGIT.matcher(password).matches()) {
            throw new BusinessException("Password must contain a number");
        }
        if (!SPECIAL.matcher(password).matches()) {
            throw new BusinessException("Password must contain a special character");
        }
    }
}
