package com.puumcore.jungophram.ecommerce.exceptions;


import com.auth0.jwt.exceptions.JWTVerificationException;
import com.puumcore.jungophram.ecommerce.custom.Assistant;
import com.puumcore.jungophram.ecommerce.models.objects.ErrorBody;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ms-customer-management
 * @version 1.x
 * @since 6/28/2024 4:21 PM
 */

@Slf4j
@ControllerAdvice
public class GlobalRestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({AccessDeniedException.class, JWTVerificationException.class})
    public ResponseEntity<ErrorBody> handleSecurityExceptions(RuntimeException ex, HttpServletRequest request) {
        HttpStatus httpStatus = HttpStatus.FORBIDDEN;
        return buildError(ex.getMessage(), httpStatus, request);
    }

    @ExceptionHandler({BadRequestException.class, AuthenticationException.class})
    public ResponseEntity<ErrorBody> handleCustomException(RuntimeException ex, HttpServletRequest request) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return buildError(ex.getMessage(), httpStatus, request);
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<ErrorBody> handleCustomException(NotFoundException ex, HttpServletRequest request) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        return buildError(ex.getMessage(), httpStatus, request);
    }

    @ExceptionHandler({FailureException.class})
    public ResponseEntity<ErrorBody> handleCustomException(FailureException ex, HttpServletRequest request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        return buildError(ex.getMessage(), httpStatus, request);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorBody> handleUncaughtExceptions(@NonNull Exception ex, HttpServletRequest request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        log.error("Uncaught error thrown", ex);

        String msg = "We ran into a problem handling your request. Check logs for more info";
        Throwable throwable = Optional.ofNullable(ex.getCause()).orElse(ex);
        if (throwable instanceof HttpMessageNotReadableException || throwable instanceof NullPointerException) {
            msg = throwable.getMessage();
        }
        return buildError(msg, httpStatus, request);
    }

    private ResponseEntity<ErrorBody> buildError(final String errorMsg, final HttpStatus httpStatus, final HttpServletRequest request) {
        final ErrorBody error = new ErrorBody(
                LocalDateTime.now(Assistant.clock),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                errorMsg,
                request == null ? null : request.getServletPath()
        );
        return new ResponseEntity<>(error, httpStatus);
    }

}
