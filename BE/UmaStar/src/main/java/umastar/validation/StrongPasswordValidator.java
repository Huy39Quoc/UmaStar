package umastar.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import umastar.util.PasswordPolicy;

/** Bộ kiểm tra của {@link StrongPassword}: ủy quyền hoàn toàn cho {@link PasswordPolicy} để chỉ có một nguồn quy tắc. */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || PasswordPolicy.isValid(value);
    }
}
