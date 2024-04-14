package com.km.commentservice.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author karanm
 */
@Data
@AllArgsConstructor
@Builder
@ToString
public class CommentReply implements Serializable {
    @Serial
    private static final long serialVersionUID = 9156473419419745370L;

    public CommentReply() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String user;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String body;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer parentId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Long replies;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Long likeCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Long dislikeCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer level;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Calcutta")
    public Date created;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Calcutta")
    public Date updated;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean isDeleted;
}
