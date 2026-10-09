package com.opao.pp_api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.*;

import com.opao.pp_api.common.constants.ValidationRegexConstants;

import io.swagger.v3.oas.annotations.media.Schema;

import com.opao.pp_api.common.constants.ValidationRangeConstants;

@Documented
@Constraint(validatedBy = {})
// 💡 FIX: Added ElementType.METHOD so Jakarta can validate Java Record accessor getters cleanly
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank(message = "Username cannot be blank")
@Pattern(regexp = ValidationRegexConstants.USERNAME_REGEX, message = "Username must be " + ValidationRangeConstants.USERNAME_MIN_LENGTH + "-" + ValidationRangeConstants.USERNAME_MAX_LENGTH + " characters long, start with a letter, and contain only alphanumeric characters or underscores")
@Schema(example = "johndoe1", description = "The unique username for the new account")
public @interface ValidUsername {
    String message() default "Invalid username";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    @Documented
    @Constraint(validatedBy = {})
    // 💡 FIX: Added ElementType.METHOD here as well for optional record field validations
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Pattern(regexp = ValidationRegexConstants.USERNAME_REGEX, message = "Username must be " + ValidationRangeConstants.USERNAME_MIN_LENGTH + "-" + ValidationRangeConstants.USERNAME_MAX_LENGTH + " characters long, start with a letter, and contain only alphanumeric characters or underscores")
    @interface Optional {
        String message() default "Invalid username";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
}
