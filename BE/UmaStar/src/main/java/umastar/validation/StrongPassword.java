package umastar.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import umastar.util.PasswordPolicy;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mật khẩu phải đạt chính sách chung của hệ thống ({@link PasswordPolicy}): từ 8 ký tự, có chữ hoa,
 * chữ thường, chữ số và tối đa 72 byte. Giá trị {@code null} / rỗng được coi là hợp lệ để
 * {@code @NotBlank} báo lỗi riêng. Dùng cho đăng ký, đổi mật khẩu và đặt lại mật khẩu.
 */
@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {

    String message() default PasswordPolicy.REQUIREMENT;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
