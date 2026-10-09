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
@NotBlank(message = "Parcel ID cannot be blank")
@Pattern(regexp = ValidationRegexConstants.PARCEL_ADDRESS_REGEX, message = "Parcel ID format is invalid (Max " + ValidationRangeConstants.PARCEL_ADDRESS_MAX_LENGTH + " alphanumeric/hyphen characters)")
public @interface ValidParcelAddress {
    String message() default "Invalid parcel ID";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    @Documented
    @Constraint(validatedBy = {})
    // 💡 FIX: Added ElementType.METHOD here as well for optional record field validations
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Pattern(regexp = ValidationRegexConstants.PARCEL_ADDRESS_REGEX, message = "Parcel ID format is invalid (Max " + ValidationRangeConstants.PARCEL_ADDRESS_MAX_LENGTH + " alphanumeric/hyphen characters)")
    @interface Optional {
        String message() default "Invalid parcel ID";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
}
