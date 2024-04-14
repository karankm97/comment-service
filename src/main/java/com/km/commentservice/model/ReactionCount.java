package com.km.commentservice.model;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

/**
 * @author karanm
 */
@Entity
@Table(name = "reaction_count")
@Data
@ToString
public class ReactionCount implements Serializable {
    @Serial
    private static final long serialVersionUID = 1721050258128779037L;

    @EmbeddedId
    private ReactionCountId id;

    @Column(name = "count", nullable = false)
    private Integer count = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id_fk", referencedColumnName = "id")
    Comment comment;
}
