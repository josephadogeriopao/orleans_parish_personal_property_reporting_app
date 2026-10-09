package com.opao.pp_api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.*;

import com.opao.pp_api.common.constants.ValidationRegexConstants;
import com.opao.pp_api.common.constants.ValidationRangeConstants;

@Documented
@Constraint(validatedBy = {})
// 💡 FIX: Added ElementType.METHOD so Jakarta can validate Java Record accessor getters cleanly
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank(message = "Full name cannot be blank")
@Pattern(regexp = ValidationRegexConstants.FULL_NAME_REGEX, message = "Full name must be "+ ValidationRangeConstants.FULL_NAME_MIN_LENGTH + "-" + ValidationRangeConstants.FULL_NAME_MAX_LENGTH + " characters long and cannot contain consecutive or trailing spaces")
public @interface ValidFullName {
    String message() default "Invalid full name";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    @Documented
    @Constraint(validatedBy = {})
    // 💡 FIX: Added ElementType.METHOD here as well for optional record field validations
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Pattern(regexp = ValidationRegexConstants.FULL_NAME_REGEX, message = "Full name must be "+ ValidationRangeConstants.FULL_NAME_MIN_LENGTH + "-" + ValidationRangeConstants.FULL_NAME_MAX_LENGTH + " characters long and cannot contain consecutive or trailing spaces")
    @interface Optional {
        String message() default "Invalid full name";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
}
