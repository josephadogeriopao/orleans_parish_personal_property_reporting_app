package com.opao.pp_api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.*;

import com.opao.pp_api.common.constants.ValidationRangeConstants;
import com.opao.pp_api.common.constants.ValidationRegexConstants;

@Documented
@Constraint(validatedBy = {})
// 💡 FIX: Added ElementType.METHOD so Jakarta can validate Java Record accessor getters cleanly
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank(message = "Phone number cannot be blank")
@Pattern(regexp = ValidationRegexConstants.PHONE_NUMBER_REGEX, message = "Phone number must be exactly "+ ValidationRangeConstants.PHONE_NUMBER_MAX_LENGTH + " digits")
public @interface ValidPhoneNumber {
    String message() default "Invalid phone number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    @Documented
    @Constraint(validatedBy = {})
    // 💡 FIX: Added ElementType.METHOD here as well for optional record field validations
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Pattern(regexp = ValidationRegexConstants.PHONE_NUMBER_REGEX, message = "Phone number must be exactly "+ ValidationRangeConstants.PHONE_NUMBER_MAX_LENGTH + " digits")
    @interface Optional {
        String message() default "Invalid phone number";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
}
