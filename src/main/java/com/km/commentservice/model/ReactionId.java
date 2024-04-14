package com.km.commentservice.model;

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
public class ReactionId implements Serializable {
    private static final long serialVersionUID = 9064756837067718516L;

    @Column(name = "comment_id", nullable = false)
    private Integer commentId;

    @Column(name = "user", nullable = false)
    private String user;
}

