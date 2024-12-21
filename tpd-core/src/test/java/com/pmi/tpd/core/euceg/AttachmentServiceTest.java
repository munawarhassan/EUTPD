package com.pmi.tpd.core.euceg;

import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.transaction.PlatformTransactionManager;

import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.core.euceg.spi.IAttachmentStore;
import com.pmi.tpd.core.euceg.spi.IProductStore;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionStore;
import com.pmi.tpd.core.euceg.spi.ISubmitterStore;
import com.pmi.tpd.core.model.euceg.AttachmentEntity;
import com.pmi.tpd.euceg.backend.core.IBackendManager;
import com.pmi.tpd.euceg.core.filestorage.IFileStorage;
import com.pmi.tpd.euceg.core.filestorage.IFileStorageFile;
import com.pmi.tpd.service.testing.junit5.AbstractServiceTest;

public class AttachmentServiceTest extends AbstractServiceTest {

    @Mock
    private IApplicationProperties applicationProperties;

    @Mock
    private IFileStorage fileStorage;

    @Mock(lenient = true)
    private IAttachmentStore attachmentStore;

    @Mock
    private ISubmitterStore submitterStore;

    @Mock(lenient = true)
    private IProductSubmissionStore productSubmissionStore;

    @Mock
    private IProductStore productStore;

    @Mock
    private IBackendManager backendManager;

    private final I18nService i18nService = new SimpleI18nService();

    @Mock
    private IEventPublisher publisher;

    @Mock
    private PlatformTransactionManager transactionManager;

    private DefaultAttachmentService attachmentService;

    public AttachmentServiceTest() {
        super(DefaultSubmissionService.class, ISubmissionService.class);
    }

    @BeforeEach
    public void setUp() throws Exception {
        attachmentService = new DefaultAttachmentService(attachmentStore, fileStorage, publisher, i18nService);
    }

    /**
     * Should remove existing attachment file
     *
     * @see TPD-161
     * @since 2.0
     */
    @Test
    public void shouldRemoveAttachment() {
        final String sendingAttachment = "c74b0e99-c18d-4250-bc56-f3564ed628fe";
        final String filename = "E-Cigarette_Nicotine_Dose_Uptake_File.pdf";
        when(attachmentStore.get(sendingAttachment))
                .thenReturn(AttachmentEntity.builder().attachmentId(sendingAttachment).filename(filename).build());
        when(attachmentStore.exists(filename)).thenReturn(true);
        when(fileStorage.existsByUuid(eq(sendingAttachment))).thenReturn(true);

        final IFileStorageFile attachment = mock(IFileStorageFile.class);
        when(attachment.getRelativeParentPath()).thenReturn(Paths.get(""));
        when(attachment.getSize()).thenReturn(10L);
        when(attachment.getName()).thenReturn(filename);
        when(attachment.getUUID()).thenReturn(sendingAttachment);
        when(fileStorage.getByUuid(eq(sendingAttachment))).thenReturn(attachment);

        attachmentService.deleteAttachment(sendingAttachment);
        verify(attachmentStore).delete(eq(sendingAttachment));
        verify(fileStorage).delete(attachment);
    }

    /**
     * Should do nothing for no existing attachment file.
     *
     * @see TPD-161
     * @since 2.0
     */
    @Test
    public void shouldDoNothingForNotExistingAttachment() {
        final String sendingAttachment = "c74b0e99-c18d-4250-bc56-f3564ed628fe";
        final String filename = "E-Cigarette_Nicotine_Dose_Uptake_File.pdf";
        when(attachmentStore.get(sendingAttachment)).thenReturn(AttachmentEntity.builder().filename(filename).build());
        when(fileStorage.existsByUuid(eq(sendingAttachment))).thenReturn(false);

        attachmentService.deleteAttachment(sendingAttachment);
        verify(attachmentStore, never()).delete(eq(sendingAttachment));
        verify(fileStorage, never()).delete(any());
    }

}
