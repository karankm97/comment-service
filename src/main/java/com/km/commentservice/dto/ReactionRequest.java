package com.km.commentservice.dto;

import com.km.commentservice.model.ReactionType;
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
public class ReactionRequest {
    @NotNull(message = "commentId is required")
    private Integer commentId;
    @NotNull(message = "user is required")
    private String user;
    @NotNull(message = "reaction type is required")
    private ReactionType reactionType;
}
