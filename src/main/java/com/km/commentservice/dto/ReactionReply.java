package com.km.commentservice.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.km.commentservice.model.ReactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * @author karanm
 */
@Builder
@Getter
@ToString
public class ReactionReply implements Serializable {
    @Serial
    private static final long serialVersionUID = -8339403706944037583L;

    @JsonProperty("pageNo")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer pageNo;

    @JsonProperty("pageSize")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer pageSize;

    @JsonProperty("size")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer size;

    @JsonProperty("commentId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer commentId;

    @JsonProperty("user")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String user;

    @JsonProperty("reactionType")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    ReactionType reactionType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Calcutta")
    public Date created;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Calcutta")
    public Date updated;

    @JsonProperty("users")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<String> users;
}
