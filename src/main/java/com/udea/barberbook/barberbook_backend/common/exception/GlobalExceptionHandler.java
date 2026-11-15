package com.udea.barberbook.barberbook_backend.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.udea.barberbook.barberbook_backend.audit.service.AuditService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final AuditService auditService;

    // HS-02 / HA-02: a client reached for an appointment that isn't theirs (or doesn't exist).
    // The answer is identical in both cases and says nothing about third parties; the attempt is
    // left in the audit trail.
    @ExceptionHandler(AppointmentAccessDeniedException.class)
    public ResponseEntity<ApiError> handleAppointmentAccessDenied(
        AppointmentAccessDeniedException ex, HttpServletRequest request
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth == null ? null : auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
            .findFirst()
            .orElse(null);
        auditService.recordForeignAppointmentAccess(
            auth != null ? auth.getName() : null, role, request.getMethod(), request.getRequestURI(),
            request.getRemoteAddr());

        ApiError body = new ApiError(
            HttpStatus.FORBIDDEN.value(), "Forbidden", ex.getMessage(), "RESERVA_NO_AUTORIZADA");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        ApiError body = new ApiError(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex) {
        ApiError body = new ApiError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiError> handleAccountLocked(AccountLockedException ex) {
        ApiError body = new ApiError(HttpStatus.LOCKED.value(), "Locked", ex.getMessage());
        return ResponseEntity.status(HttpStatus.LOCKED).body(body);
    }

    @ExceptionHandler(GoogleAuthNotConfiguredException.class)
    public ResponseEntity<ApiError> handleGoogleAuthNotConfigured(GoogleAuthNotConfiguredException ex) {
        ApiError body = new ApiError(HttpStatus.SERVICE_UNAVAILABLE.value(), "Service Unavailable", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @ExceptionHandler(InvalidGoogleTokenException.class)
    public ResponseEntity<ApiError> handleInvalidGoogleToken(InvalidGoogleTokenException ex) {
        ApiError body = new ApiError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(InvalidScheduleException.class)
    public ResponseEntity<ApiError> handleInvalidSchedule(InvalidScheduleException ex) {
        ApiError body = new ApiError(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException ex) {
        ApiError body = new ApiError(
            HttpStatus.BAD_REQUEST.value(), "Bad Request",
            "Datos inválidos: revisa el formato de los campos (ej. las horas van de 00:00 a 23:59, sin usar 24:00; "
                + "precios y duraciones son números)."
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ApiError body = new ApiError(
            HttpStatus.BAD_REQUEST.value(), "Bad Request", "Valor inválido para el parámetro '" + ex.getName() + "'."
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(MissingServletRequestParameterException ex) {
        ApiError body = new ApiError(
            HttpStatus.BAD_REQUEST.value(), "Bad Request", "Falta el parámetro '" + ex.getParameterName() + "'."
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiError> handleInvalidRequest(InvalidRequestException ex) {
        ApiError body = new ApiError(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(InvalidAvailabilityQueryException.class)
    public ResponseEntity<ApiError> handleInvalidAvailabilityQuery(InvalidAvailabilityQueryException ex) {
        ApiError body = new ApiError(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    // HU-15: the appointment is the caller's own but can't be changed now (too close to its
    // start, or already cancelled/finished). The code lets the front tell the two apart.
    @ExceptionHandler(AppointmentChangeNotAllowedException.class)
    public ResponseEntity<ApiError> handleAppointmentChangeNotAllowed(AppointmentChangeNotAllowedException ex) {
        ApiError body = new ApiError(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage(), ex.getCode());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(SlotUnavailableException.class)
    public ResponseEntity<ApiError> handleSlotUnavailable(SlotUnavailableException ex) {
        ApiError body = new ApiError(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage(), "SLOT_TAKEN");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<ApiError> handleAppointmentNotFound(AppointmentNotFoundException ex) {
        ApiError body = new ApiError(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ServiceNotFoundException.class)
    public ResponseEntity<ApiError> handleServiceNotFound(ServiceNotFoundException ex) {
        ApiError body = new ApiError(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiError> handleNotificationNotFound(NotificationNotFoundException ex) {
        ApiError body = new ApiError(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(StylistNotFoundException.class)
    public ResponseEntity<ApiError> handleStylistNotFound(StylistNotFoundException ex) {
        ApiError body = new ApiError(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        ApiError body = new ApiError(HttpStatus.BAD_REQUEST.value(), "Bad Request", "Datos inválidos", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }
}
