package com.km.commentservice.model;

import java.io.Serial;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Setter;
import lombok.ToString;

/**
 * @author karanm
 */
@Setter
@MappedSuperclass
@ToString
public abstract class BaseEntity extends BaseEntityWithoutId {
    @Serial
    private static final long serialVersionUID = -6404049885061390779L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    @JsonIgnore
    private int id;

    public Integer getId() {
        return id;
    }

}