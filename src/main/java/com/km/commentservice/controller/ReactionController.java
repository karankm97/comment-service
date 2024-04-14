package com.km.commentservice.controller;

import com.km.commentservice.dto.ReactionReply;
import com.km.commentservice.dto.ReactionRequest;
import com.km.commentservice.model.ReactionType;
import com.km.commentservice.service.ReactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * @author karanm
 */
@RestController
@RequestMapping({"/v1/comment/{commentId}/reaction", "/v1/comment/reaction"})
public class ReactionController {
    Logger logger = LoggerFactory.getLogger(ReactionController.class);

    @Autowired
    private ReactionService reactionService;

    @Cacheable(value="users", key="#commentId.toString() + '-' + #reactionType.toString() + '-' " +
            "+ #pageNo.toString() + '-' + #pageSize.toString()")
    @GetMapping(value = "{reactionType}/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReactionReply getUsersForReaction(@PathVariable("commentId") Integer commentId,
                                             @PathVariable("reactionType") ReactionType reactionType,
                                             @RequestParam(defaultValue = "0") Integer pageNo,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        logger.info("Fetching users for reaction type: {} on comment ID: {} with page number: {} and page size: {}",
                reactionType, commentId, pageNo, pageSize);
        return reactionService.getUsersForReaction(commentId, reactionType, pageNo, pageSize);
    }

    @CacheEvict(value = { "users", "fulltree", "nextlevel" }, allEntries = true)
    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReactionReply postReactionToComment(@Valid @RequestBody ReactionRequest reactionRequest) {
        logger.info("Posting a new reaction: {}", reactionRequest);
        return reactionService.postReactionToComment(reactionRequest);
    }

    @CacheEvict(value= { "users", "fulltree", "nextlevel" }, allEntries = true)
    @PatchMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReactionReply updateReaction(@Valid @RequestBody ReactionRequest reactionRequest) {
        logger.info("Updating a reaction: {}", reactionRequest);
        return reactionService.updateReactionOnComment(reactionRequest);
    }

    @CacheEvict(value= { "users", "fulltree", "nextlevel" }, allEntries = true)
    @DeleteMapping
    public Integer deleteReaction(@PathVariable("commentId") Integer commentId, @RequestParam String user) {
        logger.info("Deleting reaction from comment with ID: {} by user: {}", commentId, user);
        return reactionService.deleteReactionFromComment(commentId, user);
    }
}
