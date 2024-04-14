package com.km.commentservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author karanm
 */
@Getter
@Setter
@ToString
public class CommentRequest {
    @NotNull(message = "user is required")
    protected String user;
    @NotNull(message = "body is required")
    protected String body;
}
