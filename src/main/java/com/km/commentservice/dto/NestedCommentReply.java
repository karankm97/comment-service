package com.km.commentservice.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author karanm
 */
@Getter
@Setter
@ToString
public class NestedCommentReply implements Serializable {
    @Serial
    private static final long serialVersionUID = -646515097864001159L;

    public NestedCommentReply() {
        commentReply = null;
        nestedCommentReplies = new ArrayList<>();
    }

    @JsonProperty("comment")
    @JsonInclude(Include.NON_NULL)
    CommentReply commentReply;

    @JsonProperty("comments")
    @JsonInclude(Include.NON_EMPTY)
    List<NestedCommentReply> nestedCommentReplies;

    @JsonProperty("pageNo")
    @JsonInclude(Include.NON_NULL)
    Integer pageNo;

    @JsonProperty("pageSize")
    @JsonInclude(Include.NON_NULL)
    Integer pageSize;

    @JsonProperty("size")
    @JsonInclude(Include.NON_NULL)
    Integer size;

    @JsonProperty("maxDepth")
    @JsonInclude(Include.NON_NULL)
    Integer maxDepth;

    public NestedCommentReply withPageNo(Integer pageNo) {
        this.pageNo = pageNo;
        return this;
    }

    public NestedCommentReply withPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public NestedCommentReply withMaxDepth(Integer maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public NestedCommentReply withSize(Integer size) {
        this.size = size;
        return this;
    }

    public void addReply(NestedCommentReply nestedCommentReply) {
        nestedCommentReplies.add(nestedCommentReply);
    }
}
