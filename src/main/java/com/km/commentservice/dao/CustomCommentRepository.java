package com.km.commentservice.dao;

import java.util.List;

import com.km.commentservice.dto.CommentReply;
import org.springframework.data.domain.Pageable;

/**
 * @author karanm
 */
public interface CustomCommentRepository {
    List<CommentReply> getCommentTreeById(String path, Integer commentId, Integer maxDepth);

    List<CommentReply> getCommentsAtLevel(String commentId, Integer level, Pageable pageable);
}
