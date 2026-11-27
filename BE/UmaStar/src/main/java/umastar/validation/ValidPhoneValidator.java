package umastar.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/** Bộ kiểm tra số điện thoại Việt Nam cho {@link ValidPhone}. */
public class ValidPhoneValidator implements ConstraintValidator<ValidPhone, String> {

    public static final String VN_PHONE_REGEX = "^(?:0|\\+?84)[35789][0-9]{8}$";
    private static final Pattern VN_PHONE_PATTERN = Pattern.compile(VN_PHONE_REGEX);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || VN_PHONE_PATTERN.matcher(value).matches();
    }
}
