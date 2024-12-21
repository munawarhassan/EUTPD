package com.pmi.tpd.euceg.api.entity;

import javax.annotation.Nonnull;

import org.eu.ceg.EcigProductSubmission;
import org.eu.ceg.Submission;
import org.eu.ceg.TobaccoProductSubmission;

/**
 * <p>
 * ISubmissionVisitor interface.
 * </p>
 *
 * @param <T>
 *          a returned object type.
 * @author Christophe Friederich
 * @since 1.0
 */
public interface ISubmissionVisitor<T> {

  /**
   * @param entity
   * @return
   */
  T visit(@Nonnull ISubmissionEntity entity);

  /**
   * @param submission
   * @return
   */
  Submission visit(@Nonnull Submission submission);

  /**
   * <p>
   * visit.
   * </p>
   *
   * @param submission
   *          a {@link TobaccoProductSubmission} object.
   */
  Submission visit(@Nonnull TobaccoProductSubmission submission);

  /**
   * <p>
   * visit.
   * </p>
   *
   * @param submission
   *          a {@link EcigProductSubmission} object.
   */
  Submission visit(@Nonnull EcigProductSubmission submission);
}
