package com.km.commentservice.repository;

/**
 * @author karanm
 */

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.km.commentservice.dao.impl.CustomCommentRepositoryImpl;
import com.km.commentservice.dto.CommentReply;
import com.km.commentservice.utils.TestUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author karanm
 */
@RunWith(MockitoJUnitRunner.class)
class CustomCommentRepositoryImplTest {
    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CustomCommentRepositoryImpl customCommentRepository;

    @Mock
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCommentTreeByIdReturnsCommentReplies() throws JsonProcessingException {
        TypedQuery<CommentReply> query = mock(TypedQuery.class);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(entityManager.createQuery(anyString(), eq(CommentReply.class))).thenReturn(query);
        List<CommentReply> commentDbResponse = objectMapper.readValue(
                TestUtils.getFileContents("testing/comment-dao-flat-response.json"),
                new TypeReference<List<CommentReply>>() {});
        when(query.getResultList()).thenReturn(commentDbResponse);

        List<CommentReply> result = customCommentRepository.getCommentTreeById("path", 1, 1);

        String expectedQuery = TestUtils.getFileContents("testing/comment-tree-query.txt");

        verify(query, times(3)).setParameter(anyString(), any());
        verify(entityManager, times(1)).createQuery(expectedQuery, CommentReply.class);
        assertFalse(result.isEmpty());
    }

    @Test
    void getCommentsAtLevelReturnsCommentReplies() throws JsonProcessingException {
        TypedQuery<CommentReply> query = mock(TypedQuery.class);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(entityManager.createQuery(anyString(), eq(CommentReply.class))).thenReturn(query);
        List<CommentReply> commentDbResponse = objectMapper.readValue(
                TestUtils.getFileContents("testing/comment-dao-flat-response-single-level.json"),
                new TypeReference<List<CommentReply>>() {});

        when(query.getResultList()).thenReturn(commentDbResponse);

        Pageable pageable = PageRequest.of(0, 10);
        List<CommentReply> result = customCommentRepository.getCommentsAtLevel("commentId", 1, pageable);

        String expectedQuery = TestUtils.getFileContents("testing/comment-level-query.txt");
        verify(query, times(2)).setParameter(anyString(), any());
        verify(entityManager, times(1)).createQuery(expectedQuery, CommentReply.class);
        assertFalse(result.isEmpty());
    }
}