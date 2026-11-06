package dev.eventhub.event.api;

import dev.eventhub.event.domain.exception.EventDeletionNotAllowedException;
import dev.eventhub.event.domain.exception.EventNotFoundException;
import dev.eventhub.event.domain.exception.InsufficientSeatsException;
import dev.eventhub.event.domain.exception.InvalidCapacityException;
import dev.eventhub.event.domain.exception.InvalidEventStateException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps domain/validation exceptions to RFC 7807 {@link ProblemDetail} responses. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EventNotFoundException.class)
    public ProblemDetail handleNotFound(EventNotFoundException ex, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Event Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler({
        InvalidEventStateException.class,
        InsufficientSeatsException.class,
        EventDeletionNotAllowedException.class,
        ObjectOptimisticLockingFailureException.class
    })
    public ProblemDetail handleConflict(RuntimeException ex, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidCapacityException.class)
    public ProblemDetail handleBadRequest(InvalidCapacityException ex, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid Request", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldErrorEntry)
                .toList();
        ProblemDetail problem =
                problem(HttpStatus.BAD_REQUEST, "Validation Failed", "Request validation failed", request);
        problem.setProperty("errors", fieldErrors);
        return problem;
    }

    private Map<String, String> toFieldErrorEntry(FieldError fieldError) {
        return Map.of(
                "field",
                fieldError.getField(),
                "message",
                fieldError.getDefaultMessage() == null ? "invalid" : fieldError.getDefaultMessage());
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://eventhub.dev/problems/" + status.value()));
        problem.setProperty("path", request.getRequestURI());
        return problem;
    }
}
