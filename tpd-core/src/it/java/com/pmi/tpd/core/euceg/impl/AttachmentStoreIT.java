package com.pmi.tpd.core.euceg.impl;

import java.io.IOException;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.core.BaseDaoTestIT;
import com.pmi.tpd.core.euceg.AttachmentInvalidFilenaneException;
import com.pmi.tpd.core.euceg.ConcurrencyAttachmentAccessException;
import com.pmi.tpd.core.euceg.spi.IAttachmentRepository;
import com.pmi.tpd.core.euceg.spi.IAttachmentStore;
import com.pmi.tpd.core.model.euceg.AttachmentEntity;
import com.pmi.tpd.core.model.euceg.AttachmentRevision;
import com.pmi.tpd.euceg.api.Eucegs;

@Configuration
@ContextConfiguration(classes = { AttachmentStoreIT.class })
public class AttachmentStoreIT extends BaseDaoTestIT {

    @Inject
    private IAttachmentStore attachmentStore;

    @Bean()
    public TransactionTemplate TransactionTemplate(final PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    public IAttachmentRepository attachmentRepository(final EntityManager entityManager) {
        return new JpaAttachmentRepository(entityManager);
    }

    @Bean
    public IAttachmentStore attachmentStore(final IAttachmentRepository repository) {
        return new AttachmentStore(repository);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void shouldCreateTwoRevisionOnUpdate()
            throws IOException, ConcurrencyAttachmentAccessException, AttachmentInvalidFilenaneException {
        final String filename = "attachmentR1.pdf";
        attachmentStore.create(AttachmentEntity.builder()
                .filename(filename)
                .attachmentId(Eucegs.uuid())
                .confidential(true)
                .contentType("application/pdf")
                .build());

        final AttachmentEntity attachment = attachmentStore.getByFilename(filename);
        attachmentStore.update(attachment.copy().filename("another-name.pdf").build());
        try {
            final Page<AttachmentRevision> revisions = attachmentStore.findRevisions(attachment.getAttachmentId(),
                PageUtils.newRequest(0, 10));

            assertEquals(2, revisions.toList().size());
        } finally {
            attachmentStore.delete(attachment.getId());
        }
    }

}
