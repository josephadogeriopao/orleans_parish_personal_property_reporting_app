package com.opao.pp_api.common.exceptions;

/**
 * @author Joseph Adogeri
 * @since 06-OCT-2026
 * @version 1.0.6
 */

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.opao.pp_api.common.utils.RequestContextUtil;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Hidden
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<Map<String, Object>> handleAccountLockedException(AccountLockedException ex) {
        logSecurityContext("Account locked: " + ex.getMessage());
        Map<String, Object> errorDetails = buildErrorDetails(ex, ex.getMessage(), "ACCOUNT_LOCKED", HttpStatus.UNAUTHORIZED, false);
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(BadCredentialsException ex) {
        logSecurityContext("Invalid credentials effort: " + ex.getMessage());
        Map<String, Object> errorDetails = buildErrorDetails(ex, ex.getMessage(), "BAD_CREDENTIALS", HttpStatus.UNAUTHORIZED, false);
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        logSecurityContext("Username lookup failure: " + ex.getMessage());
        Map<String, Object> errorDetails = buildErrorDetails(ex, ex.getMessage(), "USERNAME_NOT_FOUND", HttpStatus.NOT_FOUND, false);
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, Object> errorDetails = buildErrorDetails(ex, ex.getMessage(), "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, false);
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(EntityNotFoundException ex) {
        Map<String, Object> errorDetails = buildErrorDetails(ex, ex.getMessage(), "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, false);
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflictException(ResourceConflictException ex) {
        Map<String, Object> errorDetails = buildErrorDetails(ex, ex.getMessage(), "CONFLICT", HttpStatus.CONFLICT, false);
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException ex) {
        Map<String, Object> errorDetails = buildErrorDetails(ex, ex.getMessage(), "BAD_REQUEST", HttpStatus.BAD_REQUEST, false);
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> errorDetails = buildErrorDetails(ex, ex.getMessage(), "INVALID_ARGUMENT", HttpStatus.BAD_REQUEST, false);
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationFailures(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid field format",
                        (existing, replacement) -> existing
                ));

        Map<String, Object> errorDetails = buildErrorDetails(ex, "The request submission contains invalid formatting fields.", "VALIDATION_FAILURE", HttpStatus.BAD_REQUEST, false);
        errorDetails.put("errors", fieldErrors);
        
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> errorDetails = buildErrorDetails(ex, "An unexpected internal error occurred on our infrastructure.", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, true);
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // --- PARSER-ENRICHED SERVICE METHOD CONTEXT ---

    private Map<String, Object> buildErrorDetails(Throwable throwable, String message, String appCode, HttpStatus status, boolean logStackTrace) {
        HttpServletRequest request = RequestContextUtil.getCurrentHttpRequest();
        String trackingId = UUID.randomUUID().toString();
        
        String URI = request != null ? request.getRequestURI() : "UNKNOWN_PATH";
        String HTTP_METHOD = request != null ? request.getMethod() : "UNKNOWN_METHOD";
        LocalDateTime exactServerTime = LocalDateTime.now();

        String tier = "SERVICE_BUSINESS_TIER";
        if (throwable instanceof MethodArgumentNotValidException) {
            tier = "CONTROLLER_API_TIER";
        } else if (throwable instanceof EntityNotFoundException) {
            tier = "REPOSITORY_PERSISTENCE_TIER";
        } else if (throwable instanceof SQLException || (throwable != null && throwable.getCause() instanceof SQLException)) {
            tier = "DATABASE_INFRASTRUCTURE_TIER";
        }

        try {
            MDC.put("tier", tier);
            MDC.put("trackingId", trackingId);

            if (logStackTrace) {
                log.warn("SecurityAudit - Critical infrastructure error. Method: [{}], URL: [{}], Code: [{}], Status: [{}], ExactTime: [{}], Message: [{}]", 
                        HTTP_METHOD, URI, appCode, status.value(), exactServerTime, message, throwable);
            } else {
                // 💡 1. DYNAMIC STACK TRACE PARSING ENGINE
                String causalMessage = "No message trace provided";
                if (throwable != null) {
                    StackTraceElement[] stack = throwable.getStackTrace();
                    if (stack != null && stack.length > 0) {
                        StackTraceElement origin = stack[0]; // ➔ Isolates index 0 to catch the precise breaking file line
                        causalMessage = String.format("Origin: %s.%s(Line:%d) -> %s", 
                                origin.getClassName(), 
                                origin.getMethodName(), 
                                origin.getLineNumber(), 
                                throwable.getMessage());
                    } else {
                        causalMessage = throwable.getMessage();
                    }
                }

                // 💡 2. WRITES COMPACT, ONE-LINE LOG ENRICHED WITH THE EXACT SOURCE CODE FILE ORIGIN
                log.warn("SecurityAudit - Request processing error captured. Method: [{}], URL: [{}], Code: [{}], Status: [{}], ExactTime: [{}], Message: [{}], ErrorDetail: [{}]", 
                        HTTP_METHOD, URI, appCode, status.value(), exactServerTime, message, causalMessage);
            }
            
        } finally {
            MDC.clear();
        }

        // SANITIZED CLIENT RESPONSE PAYLOAD
        Map<String, Object> details = new HashMap<>();
        details.put("trackingId", trackingId);
        details.put("message", message);
        details.put("code", appCode);
        details.put("status", status.value());
        details.put("timestamp", Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());

        return details;
    }

    private void logSecurityContext(String contextMessage) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principal = (authentication != null) ? authentication.getName() : "ANONYMOUS";
        log.info("SecurityContext Trace - Context: [{}] - Principal: [{}]", contextMessage, principal);
    }
}
