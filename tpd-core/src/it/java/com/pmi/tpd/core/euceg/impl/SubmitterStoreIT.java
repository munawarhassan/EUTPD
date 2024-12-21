package com.pmi.tpd.core.euceg.impl;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.eu.ceg.Submitter;
import org.eu.ceg.SubmitterDetails;
import org.eu.ceg.SubmitterType;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.core.BaseDaoTestIT;
import com.pmi.tpd.core.euceg.spi.ISubmitterRepository;
import com.pmi.tpd.core.euceg.spi.ISubmitterStore;
import com.pmi.tpd.core.model.euceg.SubmitterEntity;
import com.pmi.tpd.core.model.euceg.SubmitterRevision;

@Configuration
@ContextConfiguration(classes = { SubmitterStoreIT.class })
public class SubmitterStoreIT extends BaseDaoTestIT {

    @Inject
    public ISubmitterStore store;

    @Bean()
    public TransactionTemplate TransactionTemplate(final PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    public ISubmitterRepository submitterRepository(final EntityManager entityManager) {
        return new JpaSubmitterRepository(entityManager);
    }

    @Bean
    public ISubmitterStore submitterStore(final ISubmitterRepository repository) {
        return new SubmitterStore(repository);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void shouldCreateTwoRevisions() {

        final String submitterId = "10001";
        final String submitterName = "test";

        SubmitterEntity submitter = submitter(submitterId, submitterName);

        try {
            submitter = store.create(submitter);

            submitter.getSubmitterDetails().setName("new name");
            submitter.setLastModifiedDate(DateTime.now());
            store.save(submitter);

            final Page<SubmitterRevision> revisions = store.findRevisions(submitterId, PageUtils.newRequest(0, 10));

            assertEquals(2, revisions.getTotalElements());
        } finally {
            // clean up
            store.remove(submitterId);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void shouldNotSaveSubmitterOnMissingId() {
        final String submitterName = "test";

        final SubmitterEntity submitter = submitter(null, submitterName);
        try {
            store.save(submitter);
            fail();
        } catch (final JpaSystemException ex) {
        }
        assertEquals(0, store.count(), "submitter shouldn't exist");
    }

    private SubmitterEntity submitter(final String submitterId, final String submitterName) {
        return SubmitterEntity.builder()
                .submitterId(submitterId)
                .name(submitterName)
                .submitter(new Submitter().withSubmitterID(submitterId).withSubmitterType(SubmitterType.MANUFACTURER))
                .details(new SubmitterDetails().withName(submitterName))
                .lastModifiedDate(DateTime.now())
                .build();
    }
}
