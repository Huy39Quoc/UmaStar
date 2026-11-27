package umastar.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Kiểm tra số điện thoại di động Việt Nam hợp lệ: bắt đầu bằng {@code 0}, {@code 84} hoặc {@code +84},
 * theo sau bởi đầu số {@code 3, 5, 7, 8, 9} và đúng 8 chữ số.
 * Giá trị {@code null} hoặc rỗng được coi là hợp lệ (cho phép trường không bắt buộc).
 */
@Documented
@Constraint(validatedBy = ValidPhoneValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {

    String message() default "Số điện thoại Việt Nam không hợp lệ (bắt đầu bằng 0, 84 hoặc +84 và đầu số 3, 5, 7, 8, 9)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
