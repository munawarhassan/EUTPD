package com.pmi.tpd.database.hibernate.envers;

import org.hibernate.envers.DefaultRevisionEntity;
import org.springframework.data.repository.history.support.RevisionEntityInformation;

public class DefaultRevisionEntityInformation implements RevisionEntityInformation {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getRevisionNumberType() {
        return Integer.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDefaultRevisionEntity() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getRevisionEntityClass() {
        return DefaultRevisionEntity.class;
    }
}
