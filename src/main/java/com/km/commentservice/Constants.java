package com.km.commentservice;

/**
 * @author karanm
 */
public final class Constants {
    private Constants() {}

    public static final String OPERATION_NOT_ALLOWED = "Operation not allowed for given user.";
    public static final String COMMENT_NOT_FOUND = "Comment not found or is deleted.";
    public static final String PARENT_DELETED = "Parent is deleted. Cannot reply.";
    public static final String PARENT_NOT_FOUND = "Parent comment not found.";
    public static final String REACTION_NOT_FOUND = "Reaction not found for user.";
    public static final String COMMENT_DELETED_BY_USER  = "Deleted by user";
}
