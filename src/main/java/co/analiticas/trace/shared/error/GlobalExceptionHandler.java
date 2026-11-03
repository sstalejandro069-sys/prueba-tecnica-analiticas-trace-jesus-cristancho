package co.analiticas.trace.shared.error;

import co.analiticas.trace.auth.domain.InvalidCredentialsException;
import co.analiticas.trace.auth.domain.UnauthorizedException;
import co.analiticas.trace.ingestion.domain.InvalidFileContentException;
import co.analiticas.trace.ingestion.domain.InvalidFileException;
import co.analiticas.trace.ingestion.domain.UnsupportedFileTypeException;
import co.analiticas.trace.shared.domain.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Traduce excepciones de dominio y de infraestructura a respuestas {@link ApiError} coherentes. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final Map<Class<? extends DomainException>, HttpStatus> DOMAIN_STATUS = Map.of(
            InvalidCredentialsException.class, HttpStatus.UNAUTHORIZED,
            UnauthorizedException.class, HttpStatus.UNAUTHORIZED,
            InvalidFileException.class, HttpStatus.BAD_REQUEST,
            UnsupportedFileTypeException.class, HttpStatus.BAD_REQUEST,
            InvalidFileContentException.class, HttpStatus.UNPROCESSABLE_CONTENT);

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ApiError> handleDomain(DomainException e, HttpServletRequest request) {
        var status = DOMAIN_STATUS.getOrDefault(e.getClass(), HttpStatus.BAD_REQUEST);
        return build(status, e.getCode(), e.getMessage(), e.getDetails(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        var details = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "La solicitud tiene campos inválidos.", details, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "El cuerpo de la solicitud no es un JSON válido.",
                List.of(), request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiError> handleMaxSize(MaxUploadSizeExceededException e, HttpServletRequest request) {
        return build(HttpStatus.CONTENT_TOO_LARGE, "FILE_TOO_LARGE", "El archivo supera el tamaño máximo permitido (5 MB).",
                List.of(), request);
    }

    @ExceptionHandler({MissingServletRequestPartException.class, MultipartException.class})
    ResponseEntity<ApiError> handleMissingFile(Exception e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "FILE_REQUIRED", "Debe adjuntar un archivo en el campo 'file'.",
                List.of(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiError> handleNotFound(NoResourceFoundException e, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", "Recurso no encontrado.", List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception e, HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ocurrió un error inesperado. Intente de nuevo.", List.of(), request);
    }

    private static ResponseEntity<ApiError> build(HttpStatus status, String code, String message, List<String> details,
                                                  HttpServletRequest request) {
        var body = new ApiError(status.value(), code, message, details, request.getRequestURI(), Instant.now());
        return ResponseEntity.status(status).body(body);
    }
}
