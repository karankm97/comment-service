package com.km.commentservice.controller;

import com.km.commentservice.exception.ApiExceptionResponse;
import com.km.commentservice.exception.CommentServiceException;
import com.km.commentservice.exception.OperationNotAllowedException;
import com.km.commentservice.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * @author karanm
 */
@ControllerAdvice
public class ApiControllerAdvice {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiControllerAdvice.class);

    @ExceptionHandler(BindException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleBindException(BindException ex, HttpServletRequest req,
                                                    HttpServletResponse response) {
        return preExceptionHandler(ex);

    }

    @ExceptionHandler({CommentServiceException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiExceptionResponse handleCommentServiceException(CommentServiceException ex,
                                                            HttpServletRequest req, HttpServletResponse response) {
        return preExceptionHandler(ex, "Error during processing");
    }

    @ExceptionHandler({ResourceNotFoundException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiExceptionResponse handleResourceNotFoundException(ResourceNotFoundException ex,
                                                            HttpServletRequest req, HttpServletResponse response) {
        return preExceptionHandler(ex);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleResourceNotFoundException(Exception ex,
                                                                HttpServletRequest req, HttpServletResponse response) {
        return preExceptionHandler(ex, "Validation of params failed");
    }

    @ExceptionHandler({OperationNotAllowedException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiExceptionResponse handleOperationNotAllowedException(OperationNotAllowedException ex,
                                                                HttpServletRequest req, HttpServletResponse response) {
        return preExceptionHandler(ex);
    }

    @ExceptionHandler({NoResourceFoundException.class, HttpRequestMethodNotSupportedException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiExceptionResponse handleNoResourceFoundException(Exception ex,
                                                                   HttpServletRequest req, HttpServletResponse response) {
        return preExceptionHandler(ex);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiExceptionResponse handleException(Exception ex, HttpServletRequest req,
                                                HttpServletResponse response) {
        return preExceptionHandler(ex);
    }

    private ApiExceptionResponse preExceptionHandler(Exception ex) {
        return preExceptionHandler(ex, null);
    }

    private ApiExceptionResponse preExceptionHandler(Exception ex, String message) {
        ApiExceptionResponse errorResponse = new ApiExceptionResponse();
        errorResponse.setMessage(message);
        if (StringUtils.isEmpty(message)) {
            errorResponse.setMessage(ex.getMessage());
        }
        LOGGER.error(errorResponse.getErrorReferenceId(), ex);
        return errorResponse;
    }
}
