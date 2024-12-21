package com.pmi.tpd.database.support;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Persistable;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pmi.tpd.api.model.IIdentityEntity;

@MappedSuperclass
public abstract class Identifiable<K extends Serializable> implements Persistable<K>, IIdentityEntity<K> {

    /**
     * Must be {@link Transient} in order to ensure that no JPA provider complains because of a missing setter.
     */
    @Override
    @Transient
    @JsonIgnore
    public boolean isNew() {
        return null == getId();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        if (getId() == null) {
            return super.hashCode();
        }
        return ObjectUtils.nullSafeHashCode(getId());
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {

        if (null == obj) {
            return false;
        }

        if (this == obj) {
            return true;
        }
        if (!getClass().equals(Hibernate.getClass(obj))) {
            return false;
        }
        final Identifiable<?> that = (Identifiable<?>) obj;

        return null == this.getId() ? false : this.getId().equals(that.getId());
    }
}
