package com.streamhub.userservice.application.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_CHAR_PATTERN =
            Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?].*");

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 48;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) return false;

        // wyłączamy domyślny komunikat, budujemy własny
        context.disableDefaultConstraintViolation();

        if (password.length() < MIN_LENGTH) {
            buildMessage(context, "Password must be at least " + MIN_LENGTH + " characters long");
            return false;
        }
        if (password.length() > MAX_LENGTH) {
            buildMessage(context, "Password cannot exceed " + MAX_LENGTH + " characters");
            return false;
        }
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            buildMessage(context, "Password must contain at least one uppercase letter");
            return false;
        }
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            buildMessage(context, "Password must contain at least one lowercase letter");
            return false;
        }
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            buildMessage(context, "Password must contain at least one digit");
            return false;
        }
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            buildMessage(context, "Password must contain at least one special character");
            return false;
        }

        return true;
    }

    private void buildMessage(ConstraintValidatorContext context, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
