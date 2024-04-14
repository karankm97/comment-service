package com.km.commentservice.exception;

import java.io.Serial;

/**
 * @author karanm
 */
public class CommentServiceException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 7561335459365122797L;

    public CommentServiceException() {
        super();
    }

    public CommentServiceException(String message, Throwable cause, boolean enableSuppression,
                                  boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CommentServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommentServiceException(String message) {
        super(message);
    }

    public CommentServiceException(Throwable cause) {
        super(cause);
    }
}
