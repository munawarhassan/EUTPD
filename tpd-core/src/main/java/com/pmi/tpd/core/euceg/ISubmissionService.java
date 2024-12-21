package com.pmi.tpd.core.euceg;

import javax.annotation.Nonnull;

import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.euceg.api.EucegException;
import com.pmi.tpd.euceg.api.entity.IProductEntity;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;
import com.pmi.tpd.euceg.api.entity.SendSubmissionType;
import com.pmi.tpd.euceg.backend.core.IBackendManager;

/**
 * <p>
 * This interface define all business actions concerning the submission of product.
 * </p>
 * <p>
 * Here is following actions:
 * </p>
 * <ul>
 * <li>store, update and replace attachment</li>
 * <li>import tobacco and ecigarette product</li>
 * <li>import submitter</li>
 * <li>product submission</li>
 * </ul>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface ISubmissionService {

    /**
     * create a submission.
     *
     * @param entity
     *               a submission to store
     * @return Returns the persisted instance of {@link SubmissionEntity}.
     */
    @Nonnull
    SubmissionEntity createSubmission(@Nonnull ISubmissionEntity entity);

    /**
     * @param request
     * @return
     */
    SubmissionEntity createSubmission(@Nonnull final SubmissionSendRequest request);

    /**
     * create and store a submitter
     *
     * @param submitter
     *                  the submitter to create
     * @return Returns a {@link SubmitterRequest} representing the created submitter.
     * @since 2.4
     */
    @Nonnull
    SubmitterRequest createSubmitter(@Nonnull SubmitterRequest submitter);

    /**
     * Save the {@link ProductEntity entity}.
     *
     * @param request
     *                the product to update (can not be {@code null}).
     * @return Returns a persisted instance of {@link ProductEntity}.
     */
    @Nonnull
    IProductEntity saveProduct(@Nonnull ProductUpdateRequest request);

    @Nonnull
    IProductEntity updatePirStatus(@Nonnull PirStatusUpdateRequest request);

    /**
     * Persists the {@link ProductEntity entity}.
     *
     * @param entity
     *               the entity to persist (can not be {@code null}).
     * @return Returns a persisted instance of {@link ProductEntity}.
     */
    @Nonnull
    IProductEntity createProduct(@Nonnull IProductEntity entity);

    /**
     * Create a new submission for a product and send for the {@link SendSubmissionType#IMMEDIAT} submission.
     *
     * @param request
     *                a submission request to send a specific product.
     * @return Returns a newly {@link SubmissionEntity} instance.
     * @throws AttachmentIsSendingException
     *                                      occurs when try send a submission (see {@link SendSubmissionType#IMMEDIAT})
     *                                      containing attachment that is sending.
     * @throws EucegException
     *                                      occurs if the {@link IBackendManager} has not started.
     * @see SendSubmissionType
     */
    @Nonnull
    ISubmissionEntity createOrSendSubmission(@Nonnull SubmissionSendRequest request) throws EucegException;

    /**
     * @param submissionId
     *                     the unique identifier of submission to send.
     * @throws EucegException
     *                        occurs if the {@link IBackendManager} has not started.
     */
    void sendSubmission(@Nonnull Long submissionId) throws EucegException;

    /**
     * Cancel submission.
     * 
     * @param submissionId
     *                     the unique identifier of submission to cancel.
     */
    void cancelSubmission(@Nonnull Long submissionId);

    /**
     * Reject submission.
     * 
     * @param submissionId
     *                     the unique identifier of submission to cancel.
     */
    void rejectSubmission(@Nonnull Long submissionId);

    /**
     * Get the submitter for {@code submitterId}
     *
     * @param submitterId
     * @return Returns a {@link SubmitterRequest} representing the submitter associated to.
     * @since 2.4
     */
    @Nonnull
    SubmitterRequest getSubmitter(@Nonnull String submitterId);

    /**
     * Update the submitter
     *
     * @param submitter
     *                  the submitter to update
     * @return Returns a {@link SubmitterRequest} representing the updated submitter.
     * @since 2.4
     */
    @Nonnull
    SubmitterRequest updateSubmitter(@Nonnull SubmitterRequest submitter) throws EucegException;

}
