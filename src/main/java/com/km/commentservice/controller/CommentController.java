package com.km.commentservice.controller;

import com.km.commentservice.dto.CommentPutRequest;
import com.km.commentservice.dto.CommentReply;
import com.km.commentservice.dto.NestedCommentReply;
import com.km.commentservice.dto.CommentPostRequest;
import com.km.commentservice.service.CommentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author karanm
 */
@RestController
@RequestMapping("/v1/comment")
public class CommentController {
    Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    private CommentService commentService;

    @GetMapping(value = "/{commentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommentReply getCommentById(@PathVariable("commentId") Integer commentId) {
        logger.info("Fetching comment for ID: {}", commentId);
        return commentService.getCommentById(commentId);
    }

    @Cacheable(value="fulltree", key="#parentId.toString() + '-' + #maxDepth.toString()")
    @GetMapping(value = "/{parentId}/fulltree", produces = MediaType.APPLICATION_JSON_VALUE)
    public NestedCommentReply getCommentTreeByParentId(@PathVariable("parentId") Integer parentId,
                                                       @RequestParam(defaultValue = "5") Integer maxDepth) {
        logger.info("Fetching full comment tree for parent ID: {} with max depth: {}", parentId, maxDepth);
        return commentService.getCommentTreeById(parentId, maxDepth);
    }

    @Cacheable(value="nextlevel", key="#parentId.toString() + '-' + #pageNo.toString() + '-' + #pageSize.toString()")
    @GetMapping(value = "/{parentId}/nextlevel", produces = {"application/json"})
    public NestedCommentReply getCommentsAtLevel(@PathVariable("parentId") Integer parentId,
                                                 @RequestParam(defaultValue = "0") Integer pageNo,
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        logger.info("Fetching comments at level for parent ID: {} with page number: {} and page size: {}", parentId, pageNo, pageSize);
        return commentService.getCommentsAtLevel(parentId, pageNo, pageSize);
    }

    @CacheEvict(value = { "fulltree", "nextlevel" }, allEntries = true)
    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommentReply postComment(@Valid @RequestBody CommentPostRequest commentPostRequest) {
        logger.info("Posting a new comment: {}", commentPostRequest);
        return commentService.postComment(commentPostRequest);
    }

    @CacheEvict(value = { "fulltree", "nextlevel" }, allEntries = true)
    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommentReply putComment(@Valid @RequestBody CommentPutRequest commentPutRequest) {
        logger.info("Updating a comment: {}", commentPutRequest);
        return commentService.putComment(commentPutRequest);
    }

    @CacheEvict(value = { "fulltree", "nextlevel" }, allEntries = true)
    @DeleteMapping(value = "/{commentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommentReply deleteComment(@PathVariable("commentId") Integer commentId, @RequestParam String user) {
        logger.info("Deleting comment with ID: {} by user: {}", commentId, user);
        return commentService.deleteComment(commentId, user);
    }
}
