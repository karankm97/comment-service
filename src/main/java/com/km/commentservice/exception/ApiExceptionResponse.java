package com.km.commentservice.exception;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author karanm
 */
@Getter
@Setter
@JsonPropertyOrder({"errorReferenceId", "code", "message"})
public class ApiExceptionResponse {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer code;

    private String message;

    private String errorReferenceId = UUID.randomUUID().toString();
}
