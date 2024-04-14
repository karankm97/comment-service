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
public class CommentPostRequest extends CommentRequest {
    @NotNull(message = "parentId is required")
    private Integer parentId;
}
