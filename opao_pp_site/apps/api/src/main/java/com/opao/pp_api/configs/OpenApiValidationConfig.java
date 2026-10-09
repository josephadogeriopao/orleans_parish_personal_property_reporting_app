package com.opao.pp_api.configs;

import com.opao.pp_api.common.validation.*;
import com.opao.pp_api.common.constants.ValidationRangeConstants;
import com.opao.pp_api.common.constants.ValidationRegexConstants;
import io.swagger.v3.oas.models.media.Schema;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;

import org.springframework.context.annotation.Configuration;
import java.lang.annotation.Annotation;
import java.util.Iterator;

@Configuration
public class OpenApiValidationConfig implements ModelConverter {

    @Override
    public Schema<?> resolve(AnnotatedType type, 
                             ModelConverterContext context, 
                             Iterator<ModelConverter> chain) {
        
        // 1. Process the standard schema parsing sequence down the filter execution line
        Schema<?> schema = chain.hasNext() ? chain.next().resolve(type, context, chain) : null;

        // Exit immediately if the schema is unresolvable or type context lacks structural data
        if (schema == null || type.getCtxAnnotations() == null) {
            return schema;
        }

        // 2. Scan annotations array array on the active fields/parameters block
        for (Annotation annotation : type.getCtxAnnotations()) {
            Class<? extends Annotation> annotationType = annotation.annotationType();

            if (annotationType.equals(ValidUsername.class) || annotationType.equals(ValidUsername.Optional.class)) {
                schema.setType("string");
                schema.setMinLength(ValidationRangeConstants.USERNAME_MIN_LENGTH);
                schema.setMaxLength(ValidationRangeConstants.USERNAME_MAX_LENGTH);
                schema.setPattern(ValidationRegexConstants.USERNAME_REGEX);
                schema.setExample("johndoe");
                schema.setDescription("The unique account access handle. Requirement: " + ValidationRangeConstants.USERNAME_MIN_LENGTH + "-" + ValidationRangeConstants.USERNAME_MAX_LENGTH + " characters, start with a letter, alphanumeric or underscores only.");
            } 
            else if (annotationType.equals(ValidPassword.class) || annotationType.equals(ValidPassword.Optional.class)) {
                schema.setType("string");
                schema.setFormat("password");
                schema.setMinLength(ValidationRangeConstants.PASSWORD_MIN_LENGTH);
                schema.setMaxLength(ValidationRangeConstants.PASSWORD_MAX_LENGTH);
                schema.setPattern(ValidationRegexConstants.PASSWORD_REGEX);
                schema.setExample("P@ssword123!");
                schema.setDescription("Secure credential string. Requirement: " + ValidationRangeConstants.PASSWORD_MIN_LENGTH + "-" + ValidationRangeConstants.PASSWORD_MAX_LENGTH + " characters long, containing 1 uppercase, 1 lowercase, 1 digit, and 1 special symbol wrapper.");
            } 
            else if (annotationType.equals(ValidPhoneNumber.class) || annotationType.equals(ValidPhoneNumber.Optional.class)) {
                schema.setType("string");
                schema.setMinLength(ValidationRangeConstants.PHONE_NUMBER_MAX_LENGTH);
                schema.setMaxLength(ValidationRangeConstants.PHONE_NUMBER_MAX_LENGTH);
                schema.setPattern(ValidationRegexConstants.PHONE_NUMBER_REGEX);
                schema.setExample("5047548818");
                schema.setDescription("Telecommunications link identifier. Requirement: Must be a continuous sequence of exactly " + ValidationRangeConstants.PHONE_NUMBER_MAX_LENGTH + " digits without formatting punctuation.");
            } 
            else if (annotationType.equals(ValidEmail.class) || annotationType.equals(ValidEmail.Optional.class)) {
                schema.setType("string");
                schema.setFormat("email");
                schema.setMaxLength(ValidationRangeConstants.EMAIL_ADDRESS_MAX_LENGTH);
                schema.setPattern(ValidationRegexConstants.EMAIL_REGEX);
                schema.setExample("josephadogeridev@gmail.com");
                schema.setDescription("Electronic mail transmission destination routing endpoint address (Max " + ValidationRangeConstants.EMAIL_ADDRESS_MAX_LENGTH + " characters).");
            } 
            else if (annotationType.equals(ValidForeignId.class) || annotationType.equals(ValidForeignId.Optional.class)) {
                schema.setType("integer");
                schema.setFormat("int64");
                schema.setExample(1L);
                schema.setDescription("A positive numerical relational foreign database key table structural link lookup index.");
            } 
            else if (annotationType.equals(ValidFullName.class) || annotationType.equals(ValidFullName.Optional.class)) {
                schema.setType("string");
                schema.setMinLength(ValidationRangeConstants.FULL_NAME_MIN_LENGTH);
                schema.setMaxLength(ValidationRangeConstants.FULL_NAME_MAX_LENGTH);
                schema.setPattern(ValidationRegexConstants.FULL_NAME_REGEX);
                schema.setExample("Joseph Adogeri");
                schema.setDescription("Legal identifier signature layout. Cannot contain consecutive or trailing white spaces.");
            } 
            else if (annotationType.equals(ValidJurisdiction.class) || annotationType.equals(ValidJurisdiction.Optional.class)) {
                schema.setType("string");
                schema.setMaxLength(ValidationRangeConstants.JURISDICTION_MAX_LENGTH);
                schema.setPattern(ValidationRegexConstants.JURISDICTION_REGEX);
                schema.setExample("ORLEANS-001");
                schema.setDescription("Alphanumeric legal regional county block authority assessment jurisdiction identification code.");
            } 
            else if (annotationType.equals(ValidParcelAddress.class) || annotationType.equals(ValidParcelAddress.Optional.class)) {
                schema.setType("string");
                schema.setMaxLength(ValidationRangeConstants.PARCEL_ADDRESS_MAX_LENGTH);
                schema.setPattern(ValidationRegexConstants.PARCEL_ADDRESS_REGEX);
                schema.setExample("1234-7548-99-9");
                schema.setDescription("Real property geographic municipal record mapping code reference index code (Parcel Address ID).");
            }
        }

        return schema;
    }
}
