package com.km.commentservice.dao;

import java.util.Optional;

import com.km.commentservice.model.ReactionCount;
import com.km.commentservice.model.ReactionCountId;
import com.km.commentservice.model.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author karanm
 */
@Repository
public interface ReactionCountDAO extends JpaRepository<ReactionCount, Integer> {
    public Optional<ReactionCount> findById(ReactionCountId reactionCountId);
    @Modifying
    @Query("UPDATE ReactionCount " +
            "SET count = count + 1 " +
            "WHERE id.commentId = :commentId AND id.reactionType = :reactionType")
    public void incrementReactionCount(@Param("commentId") Integer commentId, @Param("reactionType") ReactionType reactionType);

    @Modifying
    @Query("UPDATE ReactionCount " +
            "SET count = count - 1 " +
            "WHERE id.commentId = :commentId AND id.reactionType = :reactionType")
    public void decrementReactionCount(@Param("commentId") Integer commentId, @Param("reactionType") ReactionType reactionType);
}
