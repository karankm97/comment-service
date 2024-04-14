package com.km.commentservice.model;

import java.io.Serial;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author karanm
 */
@Entity
@Table(name = "comment")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Comment extends BaseEntityWithoutId {
    @Serial
    private static final long serialVersionUID = -5766454318723967181L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private int id;

    @Column(name = "body")
    private String body;

    @Column(name = "parent_id")
    private int parentId;


    @Column(name = "path", columnDefinition = "TEXT")
    private String path;

    @Column(name = "level")
    private int level;

    @Column(name = "user")
    private String user;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}
