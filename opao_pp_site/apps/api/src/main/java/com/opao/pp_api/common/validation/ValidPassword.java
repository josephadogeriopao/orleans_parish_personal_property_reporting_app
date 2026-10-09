package com.opao.pp_api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.*;
import jakarta.validation.constraints.Size;

import com.opao.pp_api.common.constants.ValidationRegexConstants;
import com.opao.pp_api.common.constants.ValidationRangeConstants;

@Documented
@Constraint(validatedBy = {})
// 💡 FIX: Added ElementType.METHOD so Jakarta can validate Java Record accessor getters cleanly
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank(message = "Password cannot be blank")
@Size(min = ValidationRangeConstants.PASSWORD_MIN_LENGTH, max = ValidationRangeConstants.PASSWORD_MAX_LENGTH, message = "Password must be between {min} and {max} characters")
@Pattern(regexp = ValidationRegexConstants.PASSWORD_REGEX, message = "Password must be " + ValidationRangeConstants.PASSWORD_MIN_LENGTH + "-" + ValidationRangeConstants.PASSWORD_MAX_LENGTH + " characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character")
public @interface ValidPassword {
    String message() default "Invalid password matching rule";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    @Documented
    @Constraint(validatedBy = {})
    // 💡 FIX: Added ElementType.METHOD here as well for optional record field validations
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Pattern(regexp = ValidationRegexConstants.PASSWORD_REGEX, message = "Password must be " + ValidationRangeConstants.PASSWORD_MIN_LENGTH + "-" + ValidationRangeConstants.PASSWORD_MAX_LENGTH + " characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character")
    @interface Optional {
        String message() default "Invalid password matching rule";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
}
