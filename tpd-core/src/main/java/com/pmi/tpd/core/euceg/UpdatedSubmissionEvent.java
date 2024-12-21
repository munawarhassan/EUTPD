package com.pmi.tpd.core.euceg;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;

import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;

/**
 * Application Event indicating that a {@link ISubmissionEntity submission} has
 * been updated. This event is use mainly
 * to refresh the UI front end through WebSocket message.
 *
 * @see com.pmi.tpd.core.euceg.DefaultSubmissionService
 * @author Christophe Friederich
 * @since 1.0
 */
public final class UpdatedSubmissionEvent {

  /** */
  private final ISubmissionEntity submission;

  UpdatedSubmissionEvent(@Nonnull final ISubmissionEntity submission) {
    this.submission = checkNotNull(submission, "submission");
  }

  /**
   * Gets the updated {@link SubmissionEntity}.
   *
   * @return Returns a {@link SubmissionEntity} updated.
   */
  @Nonnull
  public ISubmissionEntity getSubmission() {
    return submission;
  }
}
