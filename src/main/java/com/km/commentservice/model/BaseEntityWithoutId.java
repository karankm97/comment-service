package com.km.commentservice.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.ToString;
//import jakarta.persistence.Version;

/**
 * @author karanm
 */
@Getter
@MappedSuperclass
@ToString
public abstract class BaseEntityWithoutId implements Serializable {
    @Serial
    private static final long serialVersionUID = -136338695112975986L;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Calcutta")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    @JsonIgnore
    private Date updatedAt = new Date();

    @PreUpdate
    public void setLastUpdate() {
        this.updatedAt = new Date();
    }
}
