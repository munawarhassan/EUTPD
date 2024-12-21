package com.pmi.tpd.euceg.api.entity;

import javax.annotation.Nonnull;

import org.eu.ceg.AttachmentAction;

import com.pmi.tpd.api.model.IAuditEntity;
import com.pmi.tpd.api.model.IInitializable;

public interface IStatusAttachment extends IInitializable, IAuditEntity {

  IStatusAttachmentId getId();

  /**
   * @return
   */
  AttachmentAction getAction();

  /**
   * @return
   */
  AttachmentSendStatus getSendStatus();

  /**
   * @return
   */
  boolean isSent();

  /**
   * @return
   */
  boolean isSending();

  Builder<? extends IStatusAttachment> copy();

  public interface Builder<T extends IStatusAttachment> {

    Builder<T> id(final IStatusAttachmentId value);

    Builder<T> action(@Nonnull final AttachmentAction action);

    Builder<T> sendStatus(final AttachmentSendStatus value);

    Builder<T> noSend();

    Builder<T> sending();

    Builder<T> sent();

    IStatusAttachment build();

  }

}
