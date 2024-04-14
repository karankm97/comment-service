package com.km.commentservice.service;

/**
 * @author karanm
 */
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.km.commentservice.dao.CommentDAO;
import com.km.commentservice.dto.CommentPostRequest;
import com.km.commentservice.dto.CommentPutRequest;
import com.km.commentservice.dto.CommentReply;
import com.km.commentservice.dto.NestedCommentReply;
import com.km.commentservice.exception.OperationNotAllowedException;
import com.km.commentservice.exception.ResourceNotFoundException;
import com.km.commentservice.model.Comment;
import com.km.commentservice.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.km.commentservice.utils.TestUtils.assertNestedReplyEqual;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author karanm
 */
@RunWith(MockitoJUnitRunner.class)
class CommentServiceTest {
    @Mock
    private CommentDAO commentDAO;

    @InjectMocks
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getSingleComment() {
        Comment comment = new Comment();
        comment.setBody("Test");
        comment.setUser("TestUser");

        when(commentDAO.findById(anyInt())).thenReturn(Optional.of(comment));

        CommentReply result = commentService.getCommentById(1);
        assertEquals(comment.getBody(), result.getBody());
        assertEquals(comment.getUser(), result.getUser());
    }

    @Test
    void getSingleCommentThrowsResourceNotFoundExceptionWhenCommentNotFound() {
        when(commentDAO.findById(anyInt())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> commentService.getCommentById(1));
    }

    @Test
    void getCommentTreeByIdReturnsNestedCommentReply() throws IOException {
        List<CommentReply> commentDbResponse = objectMapper.readValue(
                TestUtils.getFileContents("testing/comment-dao-flat-response.json"),
                new TypeReference<List<CommentReply>>() {});

        when(commentDAO.findById(anyInt())).thenReturn(Optional.of(
                new Comment(1, "test", 0, "1", 0, "km", false)));

        when(commentDAO.getCommentTreeById(anyString(), anyInt(), anyInt())).thenReturn(commentDbResponse);

        NestedCommentReply nestedCommentReply = commentService.getCommentTreeById(1, 5);
        assertNotNull(nestedCommentReply);

        NestedCommentReply expectedNestedCommentReply = objectMapper.readValue(
                TestUtils.getFileContents("testing/comment-nested-response.json"),
                NestedCommentReply.class);

        assertTrue(assertNestedReplyEqual(expectedNestedCommentReply, nestedCommentReply));
    }

    @Test
    void getCommentTreeByIdReturnsEmptyNestedCommentReplyWhenCommentNotFound() {
        when(commentDAO.findById(anyInt())).thenReturn(Optional.empty());
        NestedCommentReply nestedCommentReply =  commentService.getCommentTreeById(1, 5);
        assertTrue(assertNestedReplyEqual(new NestedCommentReply(), nestedCommentReply));
    }

    @Test
    void getCommentsAtLevelReturnsNestedCommentReply() throws JsonProcessingException {
        List<CommentReply> commentDbResponse = objectMapper.readValue(
                TestUtils.getFileContents("testing/comment-dao-flat-response-single-level.json"),
                new TypeReference<List<CommentReply>>() {});

        when(commentDAO.findById(anyInt())).thenReturn(Optional.of(
                new Comment(1, "test", 0, "1", 0, "km", false)));

        when(commentDAO.getCommentsAtLevel(anyString(), anyInt(), any())).thenReturn(commentDbResponse);

        NestedCommentReply actualNestedReply = commentService.getCommentsAtLevel(0, 0, 10);

        assertNotNull(actualNestedReply);

        NestedCommentReply expectedNestedCommentReply = objectMapper.readValue(
                TestUtils.getFileContents("testing/comment-single-level-response.json"),
                NestedCommentReply.class);

        assertTrue(assertNestedReplyEqual(expectedNestedCommentReply, actualNestedReply));
    }

    @Test
    void getCommentsAtLevelReturnsEmptyNestedCommentReplyWhenCommentNotFound() {
        when(commentDAO.findById(anyInt())).thenReturn(Optional.empty());
        NestedCommentReply nestedCommentReply =  commentService.getCommentsAtLevel(1, 0, 10);
        assertTrue(assertNestedReplyEqual(new NestedCommentReply(), nestedCommentReply));
    }

    @Test
    void postCommentReturnsNewComment() {
        CommentPostRequest request = new CommentPostRequest();
        request.setParentId(0);
        request.setBody("Test");
        request.setUser("TestUser");

        //Posting comment on top layer
        when(commentDAO.findById(anyInt())).thenReturn(Optional.empty());
        when(commentDAO.save(any(Comment.class))).thenAnswer(i -> i.getArguments()[0]);
        CommentReply result = commentService.postComment(request);
        assertEquals(request.getBody(), result.getBody());
        assertEquals(request.getUser(), result.getUser());

        //Posting comment under another comment
        request.setParentId(1);
        when(commentDAO.findById(anyInt())).thenReturn(Optional.of( new Comment(1, "test", 0, "1", 0, "km", false)));
        when(commentDAO.save(any(Comment.class))).thenAnswer(i -> i.getArguments()[0]);
        CommentReply result2 = commentService.postComment(request);
        assertEquals(request.getBody(), result2.getBody());
        assertEquals(request.getUser(), result2.getUser());
        assertEquals(request.getParentId(), result2.getParentId());
    }

    @Test
    void postCommentThrowsResourceNotFoundExceptionWhenParentNotFound() {
        CommentPostRequest request = new CommentPostRequest();
        request.setParentId(1);
        request.setBody("Test");
        request.setUser("TestUser");

        when(commentDAO.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.postComment(request));
    }

    @Test
    void putCommentUpdatesCommentBody() {
        CommentPutRequest request = new CommentPutRequest();
        request.setCommentId(1);
        request.setBody("Updated");
        request.setUser("TestUser");

        Comment existingComment = new Comment();
        existingComment.setUser("TestUser");

        when(commentDAO.findById(anyInt())).thenReturn(Optional.of(existingComment));
        when(commentDAO.save(any(Comment.class))).thenAnswer(i -> i.getArguments()[0]);

        CommentReply result = commentService.putComment(request);
        assertEquals(request.getBody(), result.getBody());
    }

    @Test
    void putCommentThrowsResourceNotFoundExceptionWhenCommentNotFound() {
        CommentPutRequest request = new CommentPutRequest();
        request.setCommentId(1);
        request.setBody("Updated");
        request.setUser("TestUser");

        when(commentDAO.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.putComment(request));
    }

    @Test
    void putCommentThrowsOperationNotAllowedExceptionWhenUserMismatch() {
        CommentPutRequest request = new CommentPutRequest();
        request.setCommentId(1);
        request.setBody("Updated");
        request.setUser("TestUser");

        Comment existingComment = new Comment();
        existingComment.setUser("DifferentUser");

        when(commentDAO.findById(anyInt())).thenReturn(Optional.of(existingComment));

        assertThrows(OperationNotAllowedException.class, () -> commentService.putComment(request));
    }

    @Test
    void deleteCommentMarksCommentAsDeleted() {
        String user = "TestUser";

        Comment existingComment = new Comment();
        existingComment.setUser(user);

        when(commentDAO.findById(anyInt())).thenReturn(Optional.of(existingComment));
        when(commentDAO.save(any(Comment.class))).thenAnswer(i -> i.getArguments()[0]);

        CommentReply result = commentService.deleteComment(1, user);
        assertTrue(result.getIsDeleted());
    }

    @Test
    void deleteCommentThrowsResourceNotFoundExceptionWhenCommentNotFound() {
        String user = "TestUser";

        when(commentDAO.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.deleteComment(1, user));
    }

    @Test
    void deleteCommentThrowsOperationNotAllowedExceptionWhenUserMismatch() {
        String user = "TestUser";

        Comment existingComment = new Comment();
        existingComment.setUser("DifferentUser");

        when(commentDAO.findById(anyInt())).thenReturn(Optional.of(existingComment));

        assertThrows(OperationNotAllowedException.class, () -> commentService.deleteComment(1, user));
    }
}