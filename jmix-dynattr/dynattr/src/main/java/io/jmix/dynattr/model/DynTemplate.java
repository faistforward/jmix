package io.jmix.dynattr.model;

import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Is a Template Category. Here you can define your Dynamic Content Templates and Copy from Here.
 * With a Template you can
 */
@JmixEntity
@Entity(name = "dynat_Template")
@Table(name = "DYNAT_TEMPLATE")
@SystemLevel
public class DynTemplate implements Categorized, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    private UUID id;
    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;
    @CreatedDate
    @Column(name = "CREATE_TS")
    private Date createTs;

    @CreatedBy
    @Column(name = "CREATED_BY", length = 50)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "UPDATE_TS")
    private Date updateTs;

    @LastModifiedBy
    @Column(name = "UPDATED_BY", length = 50)
    private String updatedBy;

    @DeletedDate
    @Column(name = "DELETE_TS")
    private Date deleteTs;

    @DeletedBy
    @Column(name = "DELETED_BY", length = 50)
    private String deletedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getCreateTs() {
        return createTs;
    }

    public void setCreateTs(Date createTs) {
        this.createTs = createTs;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(Date updateTs) {
        this.updateTs = updateTs;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getDeleteTs() {
        return deleteTs;
    }

    public void setDeleteTs(Date deleteTs) {
        this.deleteTs = deleteTs;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public void setCategory(Category category) {
        this.category = category;
    }
}