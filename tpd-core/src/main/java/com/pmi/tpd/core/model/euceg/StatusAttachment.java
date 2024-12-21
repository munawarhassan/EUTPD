package com.pmi.tpd.core.model.euceg;

import static com.pmi.tpd.api.util.Assert.notNull;

import java.io.Serializable;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.eu.ceg.AttachmentAction;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.util.ObjectUtils;

import com.google.common.base.MoreObjects;
import com.pmi.tpd.api.model.IInitializable;
import com.pmi.tpd.core.model.BaseAuditingEntity;
import com.pmi.tpd.euceg.api.entity.AttachmentSendStatus;
import com.pmi.tpd.euceg.api.entity.IStatusAttachment;
import com.pmi.tpd.euceg.api.entity.IStatusAttachmentId;
import com.pmi.tpd.euceg.api.entity.ISubmitterEntity;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Entity(name = "StatusAttachment")
@Table(name = StatusAttachment.TABLE_NAME)
@Cache(usage = CacheConcurrencyStrategy.NONE)
public class StatusAttachment extends BaseAuditingEntity<StatusAttachment.StatusAttachmentId>
        implements IInitializable, IStatusAttachment {

    /** table name. */
    public static final String TABLE_NAME = "t_status_attachment";

    /** */
    @EmbeddedId
    private StatusAttachmentId id;

    /** */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "attachment_action", nullable = false)
    private AttachmentAction action;

    /** */
    @Enumerated(EnumType.STRING)
    @Column(name = "send_status", length = 25, nullable = false)
    private AttachmentSendStatus sendStatus = AttachmentSendStatus.NO_SEND;

    /**
     * @param submitter
     * @return
     */
    public static Predicate<StatusAttachment> bySubmitter(@Nonnull final ISubmitterEntity submitter) {
        return new BySubmitter(submitter);
    }

    /**
     *
     */
    public StatusAttachment() {
    }

    @Override
    public void initialize() {

    }

    @Override
    public StatusAttachmentId getId() {
        return id;
    }

    /**
     * @return
     */
    @Override
    public AttachmentAction getAction() {
        return action;
    }

    /**
     * @return
     */
    @Override
    public AttachmentSendStatus getSendStatus() {
        return sendStatus;
    }

    /**
     * @return
     */
    @Override
    public boolean isSent() {
        return isSending() || AttachmentSendStatus.SENT.equals(sendStatus);
    }

    /**
     * @return
     */
    @Override
    public boolean isSending() {
        return AttachmentSendStatus.SENDING.equals(sendStatus);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("id", id)
                .add("action", action)
                .add("sendStatus", sendStatus)
                .toString();
    }

    /**
     * Create new {@code Builder} instance and initialise with data of the current instance.
     *
     * @return Returns new instance {@link StatusAttachment.Builder}.
     */
    @Override
    @Nonnull
    public Builder copy() {
        return new Builder(this);
    }

    /**
     * <p>
     * builder.
     * </p>
     *
     * @return a {@link StatusAttachment.Builder} object.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    public static class Builder implements IStatusAttachment.Builder<StatusAttachment> {

        /** */
        private final StatusAttachment entity;

        /**
         *
         */
        private Builder() {
            entity = new StatusAttachment();
        }

        /**
         *
         */
        private Builder(@Nonnull final StatusAttachment statusAttachment) {
            this.entity = statusAttachment;
        }

        /**
         * @param value
         * @return
         */
        @Override
        public Builder id(final IStatusAttachmentId value) {
            entity.id = (StatusAttachmentId) value;
            return self();
        }

        /**
         * @param action
         * @return
         */
        @Override
        public Builder action(@Nonnull final AttachmentAction action) {
            entity.action = action;
            return self();
        }

        /**
         * @param value
         * @return
         */
        @Override
        public Builder sendStatus(final AttachmentSendStatus value) {
            entity.sendStatus = value;
            return self();
        }

        /**
         * @return
         */
        @Override
        public Builder noSend() {
            entity.sendStatus = AttachmentSendStatus.NO_SEND;
            return self();
        }

        /**
         * @return
         */
        @Override
        public Builder sending() {
            entity.sendStatus = AttachmentSendStatus.SENDING;
            return self();
        }

        /**
         * @return
         */
        @Override
        public Builder sent() {
            entity.sendStatus = AttachmentSendStatus.SENT;
            return self();
        }

        /**
         * @return
         */
        @Override
        public StatusAttachment build() {
            return entity;
        }

        /**
         * @return
         */
        protected Builder self() {
            return this;
        }

    }

    /**
     * @author Christophe Friederich
     */
    @Embeddable
    public static final class StatusAttachmentId implements Serializable, IStatusAttachmentId {

        /** */
        private static final long serialVersionUID = -7788924822384562745L;

        /** */
        @Column(name = "attachment_id", length = 80, updatable = false)
        private String attachmentId;

        /** */
        @Column(name = "submitter_id", length = 10)
        private String submitterId;

        /**
         * @param attachment
         * @param submitter
         * @return
         */
        public static StatusAttachmentId key(final String attachment, final String submitter) {
            return new StatusAttachmentId(attachment, submitter);
        }

        /**
         *
         */
        public StatusAttachmentId() {
        }

        private StatusAttachmentId(final String attachmentId, final String submitterId) {
            this.attachmentId = attachmentId;
            this.submitterId = submitterId;
        }

        @Override
        public boolean equals(final Object otherOb) {
            if (this == otherOb) {
                return true;
            }
            if (!(otherOb instanceof StatusAttachmentId)) {
                return false;
            }
            final StatusAttachmentId other = (StatusAttachmentId) otherOb;
            return ObjectUtils.nullSafeEquals(attachmentId, other.attachmentId)
                    && ObjectUtils.nullSafeEquals(submitterId, other.submitterId);
        }

        @Override
        public int hashCode() {
            return ObjectUtils.nullSafeHashCode(attachmentId) + ObjectUtils.nullSafeHashCode(submitterId);
        }

        /**
         * @return
         */
        @Override
        public String getAttachmentId() {
            return attachmentId;
        }

        /**
         * @return
         */
        @Override
        public String getSubmitterId() {
            return submitterId;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(getClass())
                    .add("attachmentId", attachmentId)
                    .add("submitterId", submitterId)
                    .toString();
        }
    }

    /**
     * @author Christophe Friederich
     */
    private static class BySubmitter implements Predicate<StatusAttachment> {

        /** */
        private final ISubmitterEntity submitter;

        /**
         * @param submitter
         */
        public BySubmitter(@Nonnull final ISubmitterEntity submitter) {
            this.submitter = notNull(submitter);
        }

        @Override
        public boolean test(final StatusAttachment input) {
            if (input == null) {
                return false;
            }
            return submitter.getSubmitterId().equals(input.getId().getSubmitterId());
        }

    }

}
