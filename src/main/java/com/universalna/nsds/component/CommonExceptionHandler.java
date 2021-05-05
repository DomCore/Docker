package com.universalna.nsds.component;

import com.google.common.base.Throwables;
import com.universalna.nsds.exception.AlreadyExistException;
import com.universalna.nsds.exception.NotFoundException;
import com.universalna.nsds.exception.PreviewMayNotBeGeneratedException;
import com.universalna.nsds.exception.UnprocessableEntityException;
import org.hibernate.exception.JDBCConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Component
@ControllerAdvice
public class CommonExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonExceptionHandler.class);

    @Autowired
    private UUIDGenerator uuidGenerator;

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void handleAccessDeniedException(AccessDeniedException ex) {
        LOGGER.debug("AccessDeniedException: " + ex.getMessage(), ex);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public void handleMethodNotAllowedException(final HttpRequestMethodNotSupportedException e) {
        LOGGER.debug("Method not allowed: " + e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        LOGGER.debug("Bad client request: " + e.getMessage(), e);
        final Map<String, List<String>> body = e.getBindingResult().getAllErrors().stream()
                .filter(error -> error instanceof FieldError)
                .map(FieldError.class::cast)
                .collect(groupingBy(FieldError::getField, mapping(FieldError::getDefaultMessage, toList())));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException e) {
        LOGGER.debug("Bad client request: " + e.getMessage(), e);
        return new ResponseEntity<>(e.getConstraintViolations().stream().collect(Collectors.toMap(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNotFound(NotFoundException e) {
        LOGGER.debug("NotFoundException", e);
    }

    @ExceptionHandler(AlreadyExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public void handleAlreadyExist(AlreadyExistException e) {
        LOGGER.debug("AlreadyExistException", e);
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<String> handleUnprocessableEntity(UnprocessableEntityException e) {
        LOGGER.debug("UnprocessableEntityException", e);
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleBindException(final BindException ex) {
        LOGGER.debug("Bad client request: " + ex.getMessage(), ex);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        LOGGER.debug("Bad client request: " + ex.getMessage(), ex);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleMessageNotReadableException(final HttpMessageNotReadableException th) {
        LOGGER.debug("Cannot deserialize: " + th.getMessage(), th);
        return new ResponseEntity<>("malformed JSON", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PreviewMayNotBeGeneratedException.class)
    @ResponseStatus(HttpStatus.OK)
    public void handlePreviewMayNotBeGeneratedException(final PreviewMayNotBeGeneratedException th) {
        LOGGER.debug("PreviewMayNotBeGeneratedException", th);
    }

    @ExceptionHandler(JDBCConnectionException.class)
    public void handleJDBCConnectionException(final JDBCConnectionException e) {
        LOGGER.error("JDBCConnectionException", e);
        System.exit(1);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<UUID> handleThrowable(final Throwable t) {
        final UUID errorID = uuidGenerator.generate();
        LOGGER.error("errorId: " + errorID + " Unknown exception: {}, {} ", t.getMessage(), Throwables.getStackTraceAsString(t));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorID);
    }

}
