package com.km.commentservice.dao.impl;

import java.util.List;

import com.km.commentservice.dao.CustomCommentRepository;
import com.km.commentservice.dto.CommentReply;
import com.km.commentservice.model.ReactionType;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

/**
 * CustomCommentRepositoryImpl is a class that implements the CustomCommentRepository interface.
 * It provides custom methods to interact with the Comment and ReactionCount entities in the database.
 *
 * @author karanm
 */
public class CustomCommentRepositoryImpl implements CustomCommentRepository {
    Logger logger = LoggerFactory.getLogger(CustomCommentRepositoryImpl.class);

    @Autowired
    private EntityManager entityManager;

    // Prefix for the query to fetch comments and their reaction counts
    private final String prefixQuery =
            "SELECT new com.km.commentservice.dto.CommentReply(c.id as id, c.user as user, c.body as body, c.parentId as parentId, " +
                    "(SELECT COUNT(*) FROM Comment cc where cc.path LIKE (CONCAT(c.path,'%')) AND cc.path != c.path) as replies, ";

    // Dynamic infix for the query to calculate reaction counts for each reaction type
    private final String dynamicInfix = "SUM(CASE WHEN rc.id.reactionType = %d THEN rc.count ELSE 0 END) AS %sCount, ";

    // Suffix for the query to fetch comments and their reaction count
    private final String suffixQuery =
            "c.level as level, c.createdAt as created, c.updatedAt updated, c.isDeleted as isDeleted) from Comment c " +
                    "LEFT JOIN ReactionCount rc on c.id = rc.id.commentId ";

    /**
     * Fetches a tree of comments by their path and id, up to a maximum depth.
     *
     * @param path the path of the comment
     * @param commentId the id of the comment
     * @param maxDepth the maximum depth of the comment tree
     * @return a list of CommentReply objects representing the comment tree
     */
    @Override
    public List<CommentReply> getCommentTreeById(String path, Integer commentId, Integer maxDepth) {
        logger.info("Fetching comment tree by ID with path: {}, comment ID: {}, and max depth: {}",
                path, commentId, maxDepth);

        String whereClause = "where c.path LIKE (CONCAT(:path,'%')) AND c.id != :commentId AND level <= :maxDepth GROUP BY c.id ORDER BY c.path";

        return entityManager.createQuery(buildQuery(whereClause), CommentReply.class)
                .setParameter("path", path)
                .setParameter("commentId", commentId)
                .setParameter("maxDepth", maxDepth)
                .getResultList();
    }

    /**
     * Fetches comments at a specific level in the comment tree.
     *
     * @param commentId the id of the comment
     * @param level the level of the comments to fetch
     * @param pageable the pagination information
     * @return a list of CommentReply objects representing the comments at the specified level
     */
    @Override
    public List<CommentReply> getCommentsAtLevel(String commentId, Integer level, Pageable pageable) {
        logger.info("Fetching comments at level with comment ID: {}, level: {}, page number: {}, and page size: {}",
                commentId, level, pageable.getPageNumber(), pageable.getPageSize());

        String whereClause = "where c.path LIKE (CONCAT(:commentId,'%')) AND level = :level GROUP BY c.id ORDER BY c.path";

        return entityManager.createQuery(buildQuery(whereClause), CommentReply.class)
                .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .setParameter("commentId", commentId)
                .setParameter("level", level).getResultList();
    }

    /**
     * Builds a query to fetch comments and their reaction counts.
     *
     * @param whereClause the WHERE clause to use in the query
     * @return the complete query string
     */
    private String buildQuery(String whereClause) {
        logger.info("Building query with where clause: {}", whereClause);
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(prefixQuery);
        for(ReactionType reactionType : ReactionType.values()) {
            queryBuilder.append(String.format(dynamicInfix, reactionType.ordinal(), reactionType.toString().toLowerCase()));
        }
        queryBuilder.append(suffixQuery);
        queryBuilder.append(whereClause);
        logger.info("Built query with where clause: {}", queryBuilder.toString()) ;
        return queryBuilder.toString();
    }
}