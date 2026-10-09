package com.opao.pp_api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {})
// 💡 FIX: Added ElementType.METHOD so Jakarta can validate Java Record accessor getters cleanly
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@NotNull(message = "ID cannot be null")
@Positive(message = "ID must be a positive number")
public @interface ValidForeignId {
    String message() default "Invalid linked reference ID";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    @Documented
    @Constraint(validatedBy = {})
    // 💡 FIX: Added ElementType.METHOD here as well for optional relational fields
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Positive(message = "ID must be a positive number")
    @interface Optional {
        String message() default "Invalid linked reference ID";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
}
