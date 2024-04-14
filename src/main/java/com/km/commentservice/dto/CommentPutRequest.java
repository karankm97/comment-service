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
public class CommentPutRequest extends CommentRequest {
    @NotNull(message = "commentId is required")
    private Integer commentId;
}
