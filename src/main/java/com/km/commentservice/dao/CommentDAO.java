package com.km.commentservice.dao;


import java.util.List;

import com.km.commentservice.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author karanm
 */
@Repository
public interface CommentDAO extends JpaRepository<Comment, Integer>, CustomCommentRepository {
    public List<Comment> findByParentId(Integer parentId);

    public List<Comment> findByLevel(Integer integer);
}
