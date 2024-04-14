package com.km.commentservice.model;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author karanm
 */
@Getter
@Setter
@Embeddable
@ToString
public class ReactionCountId implements Serializable {
    @Serial
    private static final long serialVersionUID = -2479706211917755885L;

    @Column(name = "comment_id", nullable = false)
    private Integer commentId;

    @Column(name = "reaction_type", nullable = false)
    private ReactionType reactionType;
}
