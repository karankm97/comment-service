package com.km.commentservice.service;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.km.commentservice.CommentserviceApplication;
import com.km.commentservice.dao.CommentDAO;
import com.km.commentservice.dao.ReactionCountDAO;
import com.km.commentservice.dao.ReactionDAO;
import com.km.commentservice.dto.ReactionReply;
import com.km.commentservice.dto.ReactionRequest;
import com.km.commentservice.exception.OperationNotAllowedException;
import com.km.commentservice.exception.ResourceNotFoundException;
import com.km.commentservice.model.Comment;
import com.km.commentservice.model.Reaction;
import com.km.commentservice.model.ReactionId;
import com.km.commentservice.model.ReactionType;
import com.km.commentservice.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author karanm
 */
@RunWith(MockitoJUnitRunner.class)
class ReactionServiceTest {

    @Mock
    private ReactionDAO reactionDAO;

    @Mock
    private CommentDAO commentDAO;

    @Mock
    private ReactionCountDAO reactionCountDAO;

    @InjectMocks
    private ReactionService reactionService;

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUsersForReactionReturnsUserListWhenReactionExists() throws JsonProcessingException {
        when(commentDAO.findById(anyInt())).thenReturn(Optional.of(
                new Comment(1, "test", 0, "1", 0, "km", false)));

        List<Reaction> reactionDbResponse = objectMapper.readValue(
                TestUtils.getFileContents("testing/reaction-dao-response.json"),
                new TypeReference<List<Reaction>>() {});

        when(reactionDAO.findByIdCommentIdAndReactionTypeOrderByUpdatedAtDesc(anyInt(), any(ReactionType.class), any(Pageable.class)))
                .thenReturn(reactionDbResponse);


        ReactionReply reactionReply = reactionService.getUsersForReaction(1, ReactionType.LIKE, 0, 10);
        assertEquals(3, reactionReply.getUsers().size());
    }

    @Test
    void getUsersForReactionThrowsExceptionWhenCommentDoesNotExist() {
        when(commentDAO.findById(anyInt())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> reactionService.getUsersForReaction(1, ReactionType.LIKE, 0, 10));
    }

    @Test
    void postReactionToCommentWhenReactionDoesNotExist() {
        when(commentDAO.findById(anyInt())).thenReturn(Optional.of(
                new Comment(1, "test", 0, "1", 0, "km", false)));
        when(reactionDAO.findById(any(ReactionId.class))).thenReturn(Optional.empty());

        ReactionRequest newReactionRequest = new ReactionRequest();
        newReactionRequest.setCommentId(1);
        newReactionRequest.setReactionType(ReactionType.DISLIKE);
        newReactionRequest.setUser("km");

        ReactionId reactionId = new ReactionId();
        reactionId.setCommentId(1);
        reactionId.setUser("km");
        Reaction newReaction = new Reaction();
        newReaction.setId(reactionId);
        newReaction.setReactionType(ReactionType.DISLIKE);
        when(reactionDAO.save(any(Reaction.class))).thenReturn(newReaction);

        ReactionReply reactionReply = reactionService.postReactionToComment(newReactionRequest);
        assertEquals(newReaction.getReactionType(), reactionReply.getReactionType());
        assertEquals(newReaction.getId().getCommentId(), reactionReply.getCommentId());
        assertEquals(newReaction.getId().getUser(), reactionReply.getUser());
    }

    @Test
    void postReactionToCommentWhenReactionExists() {
        when(commentDAO.findById(anyInt())).thenReturn(Optional.of(
                new Comment(1, "test", 0, "1", 0, "km", false)));

        //different reaction as before
        ReactionId reactionId = new ReactionId();
        reactionId.setCommentId(1);
        reactionId.setUser("km");
        Reaction previousReaction = new Reaction();
        previousReaction.setId(reactionId);
        previousReaction.setReactionType(ReactionType.LIKE);
        when(reactionDAO.findById(any(ReactionId.class))).thenReturn(Optional.of(previousReaction));

        ReactionRequest newReactionRequest = new ReactionRequest();
        newReactionRequest.setCommentId(1);
        newReactionRequest.setReactionType(ReactionType.DISLIKE);
        newReactionRequest.setUser("km");

        Reaction newReaction = new Reaction();
        newReaction.setId(reactionId);
        newReaction.setReactionType(ReactionType.DISLIKE);
        when(reactionDAO.save(any(Reaction.class))).thenReturn(newReaction);

        //trying to update through post call
        assertThrows(OperationNotAllowedException.class, () -> reactionService.postReactionToComment(newReactionRequest));
    }

    @Test
    void updateReactionToCommentWhenReactionExists() {
        when(commentDAO.findById(anyInt())).thenReturn(Optional.of(
                new Comment(1, "test", 0, "1", 0, "km", false)));

        //different reaction as before
        ReactionId reactionId = new ReactionId();
        reactionId.setCommentId(1);
        reactionId.setUser("km");
        Reaction previousReaction = new Reaction();
        previousReaction.setId(reactionId);
        previousReaction.setReactionType(ReactionType.LIKE);
        when(reactionDAO.findById(any(ReactionId.class))).thenReturn(Optional.of(previousReaction));

        ReactionRequest newReactionRequest = new ReactionRequest();
        newReactionRequest.setCommentId(1);
        newReactionRequest.setReactionType(ReactionType.DISLIKE);
        newReactionRequest.setUser("km");

        Reaction newReaction = new Reaction();
        newReaction.setId(reactionId);
        newReaction.setReactionType(ReactionType.DISLIKE);
        when(reactionDAO.save(any(Reaction.class))).thenReturn(newReaction);

        ReactionReply reactionReply = reactionService.updateReactionOnComment(newReactionRequest);
        assertEquals(newReaction.getReactionType(), reactionReply.getReactionType());
        assertEquals(newReaction.getId().getCommentId(), reactionReply.getCommentId());
        assertEquals(newReaction.getId().getUser(), reactionReply.getUser());

        //same reaction as before
        newReactionRequest.setReactionType(ReactionType.LIKE);
        assertThrows(OperationNotAllowedException.class, () -> reactionService.updateReactionOnComment(newReactionRequest));
    }

    @Test
    void postReactionToCommentThrowsErrorWhenCommentIsDeleted() {
        when(commentDAO.findById(anyInt())).thenReturn(Optional.empty());
        ReactionRequest reactionRequest = new ReactionRequest();
        reactionRequest.setCommentId(1);
        reactionRequest.setReactionType(ReactionType.LIKE);
        reactionRequest.setUser("km");
        assertThrows(ResourceNotFoundException.class, () -> reactionService.postReactionToComment(reactionRequest));
    }

    @Test
    void deleteReactionFromCommentReturnsReactionIdWhenReactionExists() {
        when(commentDAO.findById(anyInt())).thenReturn(Optional.of(
                new Comment(1, "test", 0, "1", 0, "km", false)));

        ReactionId reactionId = new ReactionId();
        reactionId.setCommentId(1);
        reactionId.setUser("km");
        Reaction reaction = new Reaction();
        reaction.setId(reactionId);
        reaction.setReactionType(ReactionType.DISLIKE);
        when(reactionDAO.findById(any(ReactionId.class))).thenReturn(Optional.of(reaction));

        when(reactionDAO.deleteById(any(ReactionId.class))).thenReturn(1);
        assertEquals(1, reactionService.deleteReactionFromComment(1, "km"));
    }

    @Test
    void deleteReactionFromCommentThrowsExceptionWhenCommentDoesNotExist() {
        when(commentDAO.findById(anyInt())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> reactionService.deleteReactionFromComment(1, "km"));
    }

    @Test
    void deleteReactionFromCommentThrowsExceptionWhenReactionDoesNotExist() {
        when(commentDAO.findById(anyInt())).thenReturn(Optional.of(
                new Comment(1, "test", 0, "1", 0, "km", false)));
        when(reactionDAO.findById(any(ReactionId.class))).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> reactionService.deleteReactionFromComment(1, "user"));
    }
}
