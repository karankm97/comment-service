package com.km.commentservice.dao;


import java.util.List;
import java.util.Optional;

import com.km.commentservice.model.Reaction;
import com.km.commentservice.model.ReactionId;
import com.km.commentservice.model.ReactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author karanm
 */
@Repository
public interface ReactionDAO extends JpaRepository<Reaction, Integer> {
    public Integer deleteById(ReactionId reactionId);

    public boolean existsById(ReactionId reactionId);

    public Optional<Reaction> findById(ReactionId reactionId);

    public List<Reaction> findByIdCommentIdAndReactionTypeOrderByUpdatedAtDesc(Integer commentId, ReactionType reactionType, Pageable pageable);
}
