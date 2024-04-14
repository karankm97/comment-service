package com.km.commentservice.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.km.commentservice.dao.CommentDAO;
import com.km.commentservice.dto.CommentPutRequest;
import com.km.commentservice.dto.CommentReply;
import com.km.commentservice.dto.NestedCommentReply;
import com.km.commentservice.dto.CommentPostRequest;
import com.km.commentservice.exception.OperationNotAllowedException;
import com.km.commentservice.exception.ResourceNotFoundException;
import com.km.commentservice.model.Comment;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.km.commentservice.Constants.*;

/**
 * This service class handles all the business logic related to comments.
 * It interacts with the CommentDAO to perform CRUD operations on comments.
 * It also handles exceptions and validates the data before performing operations.
 *
 * @author karanm
 */
@Service
public class CommentService {
    Logger logger = LoggerFactory.getLogger(CommentService.class);

    @Autowired
    private CommentDAO commentDAO;

    /**
     * This method retrieves a comment by its ID.
     * It uses the CommentDAO to fetch the comment and constructs a reply.
     * @param commentId The ID of the comment.
     * @return A reply containing the data of the comment.
     */
    public CommentReply getCommentById(Integer commentId) {
        logger.info("Fetching comment with ID: {}", commentId);
        Optional<Comment> commentOptional = commentDAO.findById(commentId);
        if(commentOptional.isEmpty()) {
            logger.error("Comment with ID: {} not found", commentId);
            throw new ResourceNotFoundException(COMMENT_NOT_FOUND);
        }
        logger.info("Successfully fetched comment with ID: {}", commentId);
        return constructCommentReply(commentOptional.get(), false);
    }

    /**
     * This method retrieves a tree of comments starting from a parent comment.
     * It uses the CommentDAO to fetch the comments and constructs a nested reply.
     * @param parentId The ID of the parent comment.
     * @param maxDepth The maximum depth of the comment tree to fetch.
     * @return A nested reply of comments.
     */
    public NestedCommentReply getCommentTreeById(Integer parentId, Integer maxDepth) {
        logger.info("Fetching comment tree with parent ID: {} and max depth: {}", parentId, maxDepth);
        List<CommentReply> commentTree = new ArrayList<>();

        Optional<Comment> commentOptional = commentDAO.findById(parentId);
        if(commentOptional.isEmpty() && parentId != 0) {
            logger.error("Parent comment with ID: {} not found", parentId);
            return new NestedCommentReply();
        }

        String path = parentId != 0 ? commentOptional.get().getPath() : "";
        Integer curDepth = parentId != 0 ? commentOptional.get().getLevel() : 0;

        commentDAO.getCommentTreeById(path, parentId, curDepth + maxDepth).forEach(commentTree::add);
        logger.info("Successfully fetched comment tree with parent ID: {} and max depth: {}", parentId, maxDepth);
        return createNestedCommentReply(commentTree).withMaxDepth(maxDepth);
    }

    /**
     * This method retrieves comments at a specific level in the comment tree.
     * It uses the CommentDAO to fetch the comments and constructs a nested reply.
     * @param parentId The ID of the parent comment.
     * @param pageNo The page number for pagination.
     * @param pageSize The size of the page for pagination.
     * @return A nested reply of comments.
     */
    public NestedCommentReply getCommentsAtLevel(Integer parentId, Integer pageNo, Integer pageSize) {
        logger.info("Fetching comments at level for parent ID: {} with page number: {} and page size: {}",
                parentId, pageNo, pageSize);
        List<CommentReply> commentTree = new ArrayList<>();

        Optional<Comment> commentOptional = commentDAO.findById(parentId);
        if(commentOptional.isEmpty() && parentId != 0) {
            logger.error("Parent comment with ID: {} not found", parentId);
            return new NestedCommentReply();
        }

        String path = parentId != 0 ? commentOptional.get().getPath() : "";
        int level = parentId != 0 ? commentOptional.get().getLevel() : -1;
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        commentDAO.getCommentsAtLevel(path, level+1, pageable).forEach(commentTree::add);
        logger.info("Successfully fetched comments at level for parent ID: {} with page number: {} and page size: {}",
                parentId, pageNo, pageSize);
        return createNestedCommentReply(commentTree).withPageNo(pageNo).withPageSize(pageSize).withSize(commentTree.size());
    }

    /**
     * This method posts a new comment.
     * It validates the data, creates a new Comment object, and uses the CommentDAO to save it.
     * @param commentPostRequest The request object containing the data for the new comment.
     * @return A reply containing the data of the new comment.
     */
    @Transactional
    public CommentReply postComment(CommentPostRequest commentPostRequest) {
        logger.info("Posting a new comment: {}", commentPostRequest);
        Optional<Comment> parentCommentOptional = commentDAO.findById(commentPostRequest.getParentId());
        Comment parentComment = parentCommentOptional.orElse(null);

        Comment comment = new Comment();
        comment.setBody(commentPostRequest.getBody());
        comment.setParentId(commentPostRequest.getParentId());
        comment.setUser(commentPostRequest.getUser());

        if (parentComment != null) {
            if(parentComment.getIsDeleted()) {
                logger.error("Parent comment with ID: {} is deleted", commentPostRequest.getParentId());
                throw new ResourceNotFoundException(PARENT_DELETED);
            }
            comment.setLevel(parentCommentOptional.get().getLevel() + 1);
        } else if (commentPostRequest.getParentId() == 0){
            //Allowing to add parent at top most level
            comment.setLevel(0);
        } else {
            logger.error("Parent comment with ID: {} not found", commentPostRequest.getParentId());
            throw new ResourceNotFoundException(PARENT_NOT_FOUND);
        }

        Comment newComment = commentDAO.save(comment);
        newComment.setPath(parentComment != null ? parentComment.getPath() + "-" + newComment.getId(): new String()
                + newComment.getId());
        logger.info("Successfully posted a new comment with ID: {} and updating path with: {}", newComment.getId(),
                newComment.getPath());
        return constructCommentReply(commentDAO.save(newComment), false);
    }

    /**
     * This method updates an existing comment.
     * It validates the data, fetches the Comment object, updates it, and uses the CommentDAO to save it.
     * @param commentPutRequest The request object containing the data for the comment update.
     * @return A reply containing the data of the updated comment.
     */
    @Transactional
    public CommentReply putComment(CommentPutRequest commentPutRequest) {
        logger.info("Updating a comment: {}", commentPutRequest);
        Optional<Comment> commentOptional = commentDAO.findById(commentPutRequest.getCommentId());

        Comment comment = commentOptional.orElse(null);

        if(comment == null || comment.getIsDeleted()) {
            logger.error("Comment with ID: {} not found or is deleted", commentPutRequest.getCommentId());
            throw new ResourceNotFoundException(COMMENT_NOT_FOUND);
        }
        if(!commentPutRequest.getUser().equals(comment.getUser())) {
            logger.error("Operation not allowed for user: {}", commentPutRequest.getUser());
            throw new OperationNotAllowedException(OPERATION_NOT_ALLOWED);
        }

        comment.setBody(commentPutRequest.getBody());
        Comment updatedComment = commentDAO.save(comment);
        logger.info("Successfully updated comment with ID: {}", updatedComment.getId());
        return constructCommentReply(updatedComment, true);
    }

    /**
     * This method deletes a comment.
     * It validates the data, fetches the Comment object, marks it as deleted, and uses the CommentDAO to save it.
     * @param commentId The ID of the comment to delete.
     * @param user The user who is performing the delete operation.
     * @return A reply containing the data of the deleted comment.
     */
    @Transactional
    public CommentReply deleteComment(Integer commentId, String user) {
        logger.info("Deleting comment with ID: {} by user: {}", commentId, user);
        Optional<Comment> commentOptional = commentDAO.findById(commentId);

        Comment comment = commentOptional.orElse(null);
        if(comment == null || comment.getIsDeleted()) {
            logger.error("Comment with ID: {} not found or is deleted", commentId);
            throw new ResourceNotFoundException(COMMENT_NOT_FOUND);
        }
        if(!user.equals(comment.getUser())) {
            logger.error("Operation not allowed for user: {}", user);
            throw new OperationNotAllowedException(OPERATION_NOT_ALLOWED);
        }

        comment.setBody(COMMENT_DELETED_BY_USER);
        comment.setIsDeleted(true);

        Comment deletedComment = commentDAO.save(comment);
        logger.info("Successfully deleted comment with ID: {} by user: {}", commentId, user);
        return constructCommentReply(deletedComment, true);
    }

    /**
     * This method constructs a CommentReply object from a Comment object.
     * @param comment The Comment object.
     * @param isUpdate A flag indicating whether the comment is being updated.
     * @return A CommentReply object.
     */
    private CommentReply constructCommentReply(Comment comment, Boolean isUpdate) {
        CommentReply commentReply = CommentReply.builder()
                .id(comment.getId())
                .body(comment.getBody())
                .user(comment.getUser())
                .parentId(comment.getParentId())
                .isDeleted(comment.getIsDeleted())
                .build();
        if(isUpdate) {
            commentReply.setUpdated(comment.getUpdatedAt());
        } else {
            commentReply.setCreated(comment.getCreatedAt());
        }
        return commentReply;
    }

    /**
     * This method creates a nested reply of comments from a list of CommentReply objects.
     * @param commentReplies The list of CommentReply objects.
     * @return A nested reply of comments.
     */
    private NestedCommentReply createNestedCommentReply(List<CommentReply> commentReplies) {
        logger.info("Creating nested comment reply for a list of comment replies");
        Map<Integer, List<CommentReply>> parentAdjList = new HashMap<>();
        Map<Integer, CommentReply> commentReplyMap = new HashMap<>();

        for (CommentReply commentReply : commentReplies) {
            if(!parentAdjList.containsKey(commentReply.getParentId())) {
                parentAdjList.put(commentReply.getParentId(), new ArrayList<>());
            }
            parentAdjList.get(commentReply.getParentId()).add(commentReply);
            commentReplyMap.put(commentReply.getId(), commentReply);
        }

        NestedCommentReply topReply = new NestedCommentReply();

        Map<Integer, NestedCommentReply> setOfReplies = new HashMap<>();

        for(Map.Entry<Integer, List<CommentReply>> mapEntry : parentAdjList.entrySet()) {
            NestedCommentReply nestedReply = setOfReplies.getOrDefault(mapEntry.getKey(), new NestedCommentReply());
            if(commentReplyMap.containsKey(mapEntry.getKey())) {
                nestedReply.setCommentReply(commentReplyMap.get(mapEntry.getKey()));
            } else {
                topReply = nestedReply;
            }
            for(CommentReply commentReply : mapEntry.getValue()) {
                NestedCommentReply cn = new NestedCommentReply();
                cn.setCommentReply(commentReply);
                nestedReply.addReply(cn);
                setOfReplies.put(commentReply.getId(), cn);
            }
        }
        logger.info("Successfully created nested comment reply");
        return topReply;
    }
}