package com.km.commentservice.exception;

import java.io.Serial;

/**
 * @author karanm
 */
public class OperationNotAllowedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 7402943516596563123L;

    public OperationNotAllowedException() {
        super();
    }

    public OperationNotAllowedException(String message, Throwable cause, boolean enableSuppression,
                                     boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public OperationNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    public OperationNotAllowedException(String message) {
        super(message);
    }

    public OperationNotAllowedException(Throwable cause) {
        super(cause);
    }
}
