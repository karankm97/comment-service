package com.km.commentservice.model;

import java.io.Serial;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

/**
 * @author karanm
 */
@Entity
@Table(name = "reaction")
@Data
@ToString
public class Reaction extends BaseEntityWithoutId {
    @Serial
    private static final long serialVersionUID = 8156907108870582205L;

    @EmbeddedId
    private ReactionId id;

    @Column(name = "reaction_type")
    private ReactionType reactionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id_fk", referencedColumnName = "id")
    Comment comment;
}
