package com.pmi.tpd.euceg.api.entity;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eu.ceg.AttachmentAction;

import com.pmi.tpd.api.model.IAuditEntity;
import com.pmi.tpd.api.model.IIdentityEntity;
import com.pmi.tpd.api.model.IInitializable;

public interface IAttachmentEntity extends IInitializable, IIdentityEntity<String>, IAuditEntity {

    /**
     * @return Returns the current version of attachment.
     * @since 2.4
     */
    int getVersion();

    /**
     * @return Returns the identifier.
     * @see #getId()
     */
    String getAttachmentId();

    /**
     * @return Returns the file name.
     */
    @Nonnull
    String getFilename();

    /**
     * Gets the {@link AttachmentAction} for the specific {@code submitter}.
     *
     * @param submitter
     *                  the submitter to user.
     * @return Returns a optional value representing the {@link AttachmentAction} for the specific {@code submitter}.
     */
    AttachmentAction getAction(@Nonnull ISubmitterEntity submitter);

    /**
     * @return Returns the content type.
     */
    @Nullable
    String getContentType();

    /**
     * @return Returns {@code true} whether is flagged as confidential, {@code false} otherwise.
     */
    boolean isConfidential();

    /**
     * @return Returns the status.
     */
    Set<? extends IStatusAttachment> getStatus();

    Optional<IStatusAttachment> getDefaultStatus();

    /**
     * Gets the {@link StatusAttachment} for the specific {@code submitter}.
     *
     * @param submitter
     *                  the submitter to user.
     * @return Returns a optional value representing the {@link StatusAttachment} for the specific {@code submitter}.
     */
    Optional<IStatusAttachment> getStatus(@Nonnull ISubmitterEntity submitter);

    /**
     * Gets the indicating whether is sent for the specific {@code submitter}.
     *
     * @param submitter
     *                  the submitter to user.
     * @return Returns {@code true} whether is sent for the specific {@code submitter}, {@code false} otherwise.
     */
    boolean isSent(@Nonnull ISubmitterEntity submitter);

    /**
     * Gets the indicating whether is sending for the specific {@code submitter}.
     *
     * @param submitter
     *                  the submitter to user.
     * @return Returns {@code true} whether is sending for the specific {@code submitter}, {@code false} otherwise.
     */
    boolean isSending(@Nonnull ISubmitterEntity submitter);

}
