package com.km.commentservice.service;

import java.util.List;
import java.util.Optional;

import com.km.commentservice.dao.CommentDAO;
import com.km.commentservice.dao.ReactionCountDAO;
import com.km.commentservice.dao.ReactionDAO;
import com.km.commentservice.dto.ReactionReply;
import com.km.commentservice.dto.ReactionRequest;
import com.km.commentservice.exception.OperationNotAllowedException;
import com.km.commentservice.exception.ResourceNotFoundException;
import com.km.commentservice.model.Comment;
import com.km.commentservice.model.Reaction;
import com.km.commentservice.model.ReactionCount;
import com.km.commentservice.model.ReactionCountId;
import com.km.commentservice.model.ReactionId;
import com.km.commentservice.model.ReactionType;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.km.commentservice.Constants.COMMENT_NOT_FOUND;
import static com.km.commentservice.Constants.REACTION_NOT_FOUND;

/**
 * Service class for managing reactions in a comment system.
 * It provides methods for getting users for a reaction, posting a reaction to a comment, and deleting a reaction from a comment.
 * It also contains helper methods for managing the count of reactions.
 *
 * @author karanm
 */
@Service
public class ReactionService {
    Logger logger = LoggerFactory.getLogger(ReactionService.class);

    @Autowired
    private ReactionDAO reactionDAO;

    @Autowired
    private CommentDAO commentDAO;

    @Autowired
    private ReactionCountDAO reactionCountDAO;

    /**
     * Retrieves a list of users who have reacted to a specific comment with a specific reaction type.
     *
     * @param commentId the ID of the comment
     * @param reactionType the type of the reaction
     * @param pageNo the page number for pagination
     * @param pageSize the size of the page for pagination
     * @return a list of usernames
     * @throws ResourceNotFoundException if the comment does not exist or is deleted
     */
    public ReactionReply getUsersForReaction(Integer commentId, ReactionType reactionType, Integer pageNo, Integer pageSize) {
        logger.info("Fetching users for reaction on comment ID: {} with reaction type: {} on page number: {} with page size: {}",
                commentId, reactionType, pageNo, pageSize);
        Optional<Comment> commentOptional = commentDAO.findById(commentId);

        if (commentOptional.isEmpty() || commentOptional.get().getIsDeleted()) {
            logger.error("Comment with ID: {} not found or is deleted", commentId);
            throw new ResourceNotFoundException(COMMENT_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        List<Reaction> reactions = reactionDAO.findByIdCommentIdAndReactionTypeOrderByUpdatedAtDesc(commentId, reactionType, pageable);
        List<String> users = reactions.stream().map(Reaction::getId).map(ReactionId::getUser).toList();

        logger.info("Successfully fetched users for reaction on comment ID: {} with reaction type: {} on page number: " +
                "{} with page size: {}", commentId, reactionType, pageNo, pageSize);
        return ReactionReply.builder().users(users).pageNo(pageNo).pageSize(pageSize).size(users.size()).build();
    }

    /**
     * Allows a user to post a reaction to a comment.
     *
     * @param reactionRequest the reaction request containing the comment ID, reaction type, and user
     * @return the posted reaction
     * @throws ResourceNotFoundException if the comment does not exist or is deleted
     * @throws OperationNotAllowedException if the reaction is the same as before
     */
    @Transactional
    public ReactionReply postReactionToComment(ReactionRequest reactionRequest) {
        Integer commentId = reactionRequest.getCommentId();
        ReactionType reactionType = reactionRequest.getReactionType();
        String user = reactionRequest.getUser();

        logger.info("Posting reaction to comment ID: {} with reaction type: {} by user: {}", commentId, reactionType, user);

        Optional<Comment> commentOptional = commentDAO.findById(commentId);

        if (commentOptional.isEmpty() || commentOptional.get().getIsDeleted()) {
            logger.error("Comment with ID: {} not found or is deleted", commentId);
            throw new ResourceNotFoundException(COMMENT_NOT_FOUND);
        }

        //TODO: Object creation needs to be simplified. Use builder pattern.
        ReactionId reactionId = new ReactionId();
        reactionId.setCommentId(commentId);
        reactionId.setUser(user);
        Optional<Reaction> previousReaction = reactionDAO.findById(reactionId);

        //New reaction
        Reaction newReaction = new Reaction();
        newReaction.setId(reactionId);
        newReaction.setReactionType(reactionType);

        if(previousReaction.isPresent()) {
            logger.error("Reaction already exists for comment ID: {} by user: {}", commentId, user);
            throw new OperationNotAllowedException("Reaction already exists");
        } else {
            insertReactionCount(newReaction);
            Reaction reply =  reactionDAO.save(newReaction);
            logger.info("Successfully posted reaction to comment ID: {} with reaction type: {} by user: {}",
                    commentId, reactionType, user);
            return ReactionReply.builder().commentId(reply.getId().getCommentId()).user(reply.getId().getUser())
                    .reactionType(reply.getReactionType()).created(reply.getCreatedAt()).build();
        }
    }

    /**
     * Allows a user to update their reaction on a comment.
     *
     * @param reactionRequest the reaction request containing the comment ID, reaction type, and user
     * @return the updated reaction
     * @throws ResourceNotFoundException if the comment does not exist or is deleted
     * @throws OperationNotAllowedException if the reaction is the same as before
     */
    @Transactional
    public ReactionReply updateReactionOnComment(ReactionRequest reactionRequest) {
        //TODO: Templatize the class to merge post and update methods common functionality
        Integer commentId = reactionRequest.getCommentId();
        ReactionType reactionType = reactionRequest.getReactionType();
        String user = reactionRequest.getUser();

        logger.info("Updating reaction to comment ID: {} with reaction type: {} by user: {}", commentId, reactionType, user);

        Optional<Comment> commentOptional = commentDAO.findById(commentId);

        if (commentOptional.isEmpty() || commentOptional.get().getIsDeleted()) {
            logger.error("Comment with ID: {} not found or is deleted", commentId);
            throw new ResourceNotFoundException(COMMENT_NOT_FOUND);
        }

        ReactionId reactionId = new ReactionId();
        reactionId.setCommentId(commentId);
        reactionId.setUser(user);
        Optional<Reaction> previousReaction = reactionDAO.findById(reactionId);

        //New reaction
        Reaction newReaction = new Reaction();
        newReaction.setId(reactionId);
        newReaction.setReactionType(reactionType);

        if(previousReaction.isPresent()) {
            if (previousReaction.get().getReactionType() == newReaction.getReactionType()) {
                logger.error("Reaction is same as before for comment ID: {} by user: {}", commentId, user);
                throw new OperationNotAllowedException("Reaction is same as before.");
            } else {
                updateReactionCount(newReaction, previousReaction.get());
                Reaction reply = reactionDAO.save(newReaction);
                logger.info("Successfully updated reaction to comment ID: {} with reaction type: {} by user: {}",
                        commentId, reactionType, user);
                return ReactionReply.builder().commentId(reply.getId().getCommentId()).user(reply.getId().getUser())
                        .reactionType(reply.getReactionType()).updated(reply.getUpdatedAt()).build();
            }
        } else {
            logger.error("Reaction not found for comment ID: {} by user: {}", commentId, user);
            throw new ResourceNotFoundException(REACTION_NOT_FOUND);
        }
    }

    /**
     * Allows a user to delete their reaction from a comment.
     *
     * @param commentId the ID of the comment
     * @param user the user who posted the reaction
     * @return the ID of the deleted reaction
     * @throws ResourceNotFoundException if the comment or reaction does not exist
     */
    @Transactional
    public Integer deleteReactionFromComment(Integer commentId, String user) {
        logger.info("Deleting reaction from comment ID: {} by user: {}", commentId, user);

        ReactionId reactionId = new ReactionId();
        reactionId.setCommentId(commentId);
        reactionId.setUser(user);

        Optional<Comment> commentOptional = commentDAO.findById(commentId);
        if(commentOptional.isEmpty() || commentOptional.get().getIsDeleted()) {
            logger.error("Comment with ID: {} not found or is deleted", commentId);
            throw new ResourceNotFoundException(COMMENT_NOT_FOUND);
        }

        Optional<Reaction> reaction = reactionDAO.findById(reactionId);
        if(reaction.isEmpty()) {
            logger.error("Reaction not found for comment ID: {} by user: {}", commentId, user);
            throw new ResourceNotFoundException(REACTION_NOT_FOUND);
        }

        Integer reactionReply = reactionDAO.deleteById(reactionId);
        deleteReactionCount(reaction.get());
        logger.info("Successfully deleted reaction from comment ID: {} by user: {}", commentId, user);
        return reactionReply;
    }

    /**
     * Updates the count of reactions.
     *
     * @param newReaction the new reaction
     * @param previousReaction the previous reaction
     */
    private void updateReactionCount(Reaction newReaction, Reaction previousReaction) {
        insertReactionCount(newReaction);
        reactionCountDAO.decrementReactionCount(previousReaction.getId().getCommentId(), previousReaction.getReactionType());
    }

    /**
     * Decrements the count of reactions.
     *
     * @param reaction the reaction to be deleted
     */
    private void deleteReactionCount(Reaction reaction) {
        reactionCountDAO.decrementReactionCount(reaction.getId().getCommentId(), reaction.getReactionType());
    }

    /**
     * Inserts a new reaction count.
     *
     * @param reaction the reaction to be inserted
     */
    private void insertReactionCount(Reaction reaction) {
        ReactionCountId reactionCountId = new ReactionCountId();
        reactionCountId.setCommentId(reaction.getId().getCommentId());
        reactionCountId.setReactionType(reaction.getReactionType());
        Optional<ReactionCount> reactionCountOptional = reactionCountDAO.findById(reactionCountId);
        if(reactionCountOptional.isPresent()) {
            reactionCountDAO.incrementReactionCount(reaction.getId().getCommentId(), reaction.getReactionType());
        } else {
            ReactionCount reactionCount = new ReactionCount();
            reactionCount.setId(reactionCountId);
            reactionCount.setCount(1);
            reactionCountDAO.save(reactionCount);
        }
    }
}
