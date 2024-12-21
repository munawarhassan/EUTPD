package com.pmi.tpd.core.euceg.impl;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Repository;

import com.pmi.tpd.core.euceg.spi.IAttachmentRepository;
import com.pmi.tpd.core.model.euceg.AttachmentEntity;
import com.pmi.tpd.core.model.euceg.QAttachmentEntity;
import com.pmi.tpd.database.hibernate.envers.DefaultJpaEnversRevisionRepository;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Repository
public class JpaAttachmentRepository extends DefaultJpaEnversRevisionRepository<AttachmentEntity, String, Integer>
        implements IAttachmentRepository {

    /**
     * @param entityManager
     *                      the JPA entity manager.
     */
    public JpaAttachmentRepository(final EntityManager entityManager) {
        super(AttachmentEntity.class, entityManager);
    }

    @Override
    public QAttachmentEntity entity() {
        return QAttachmentEntity.attachmentEntity;
    }

    @Override
    @Nonnull
    public AttachmentEntity getByFilename(@Nonnull final String filename) {
        AttachmentEntity entity = from().where(entity().filename.eq(filename)).fetchOne();
        if (entity == null) {
            throw new EntityNotFoundException("the attachment with filename '" + filename + "' doesn't exits.");
        }
        return entity;
    }

    @Override
    @Nonnull
    public Optional<AttachmentEntity> findByFilename(@Nonnull final String filename) {
        try {
            return this.findOne(entity().filename.eq(filename));
        } catch (IncorrectResultSizeDataAccessException ex) {
            throw new IncorrectResultSizeDataAccessException("try find '" + filename + "'", ex.getExpectedSize(),
                    ex.getActualSize(), ex);
        }
    }

}
