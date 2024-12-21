package com.pmi.tpd.core.euceg.impl;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import com.pmi.tpd.core.euceg.spi.ISubmitterRepository;
import com.pmi.tpd.core.model.euceg.QSubmitterEntity;
import com.pmi.tpd.core.model.euceg.SubmitterEntity;
import com.pmi.tpd.database.hibernate.envers.DefaultJpaEnversRevisionRepository;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Repository
public class JpaSubmitterRepository extends DefaultJpaEnversRevisionRepository<SubmitterEntity, String, Integer>
        implements ISubmitterRepository {

    /**
     * @param entityManager
     *            the JPA entity manager.
     */
    public JpaSubmitterRepository(final EntityManager entityManager) {
        super(SubmitterEntity.class, entityManager);
    }

    @Override
    public QSubmitterEntity entity() {
        return QSubmitterEntity.submitterEntity;
    }

}
