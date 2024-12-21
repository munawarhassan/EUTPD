package com.pmi.tpd.core.euceg.spi;

import javax.annotation.Nonnull;

import com.pmi.tpd.core.model.euceg.QSubmitterEntity;
import com.pmi.tpd.core.model.euceg.SubmitterEntity;
import com.pmi.tpd.database.hibernate.envers.IEnversRevisionRepository;
import com.pmi.tpd.database.jpa.IDslAccessor;

/**
 * <p>
 * ISubmitterRepository interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface ISubmitterRepository
        extends IDslAccessor<SubmitterEntity, String>, IEnversRevisionRepository<SubmitterEntity, String, Integer> {

    @Override
    @Nonnull
    QSubmitterEntity entity();

}
