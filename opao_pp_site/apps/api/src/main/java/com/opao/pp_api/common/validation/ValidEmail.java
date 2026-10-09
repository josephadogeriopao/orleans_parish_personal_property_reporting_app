package com.opao.pp_api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.lang.annotation.*;

import com.opao.pp_api.common.constants.ValidationRegexConstants;
import com.opao.pp_api.common.constants.ValidationRangeConstants;

@Documented
@Constraint(validatedBy = {})
// 💡 FIX: Added ElementType.METHOD so Jakarta can validate Java Record accessor getters cleanly [3, 4]
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank(message = "Email address cannot be blank")
@Pattern(regexp = ValidationRegexConstants.EMAIL_REGEX, message = "Invalid email address format")
@Size(max = ValidationRangeConstants.EMAIL_ADDRESS_MAX_LENGTH, message = "Email address exceeds maximum allowed length")
public @interface ValidEmail {
    String message() default "Invalid email address";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    @Documented
    @Constraint(validatedBy = {})
    // 💡 FIX: Added ElementType.METHOD here as well for optional record field validations [3, 4]
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Pattern(regexp = ValidationRegexConstants.EMAIL_REGEX, message = "Invalid email address format")
    @Size(max = ValidationRangeConstants.EMAIL_ADDRESS_MAX_LENGTH, message = "Email address exceeds maximum allowed length")
    @interface Optional {
        String message() default "Invalid email address";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
}
