package com.pmi.tpd.core.euceg.spi;

import java.util.Optional;

import javax.annotation.Nonnull;

import com.pmi.tpd.core.model.euceg.AttachmentEntity;
import com.pmi.tpd.core.model.euceg.QAttachmentEntity;
import com.pmi.tpd.database.hibernate.envers.IEnversRevisionRepository;
import com.pmi.tpd.database.jpa.IDslAccessor;

/**
 * <p>
 * IAttachmentRepository interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IAttachmentRepository
        extends IDslAccessor<AttachmentEntity, String>, IEnversRevisionRepository<AttachmentEntity, String, Integer> {

    /**
     * @param filename
     *                 the file name corresponding to attachment.
     * @return Returns the {@link AttachmentEntity} associated to the {@code filename}.
     */
    @Nonnull
    AttachmentEntity getByFilename(@Nonnull String filename);

    /**
     * @param filename
     * @return
     */
    @Nonnull
    Optional<AttachmentEntity> findByFilename(final @Nonnull String filename);

    @Override
    QAttachmentEntity entity();

}
