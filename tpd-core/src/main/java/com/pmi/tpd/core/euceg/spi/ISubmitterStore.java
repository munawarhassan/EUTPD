package com.pmi.tpd.core.euceg.spi;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.core.model.euceg.SubmitterDifference;
import com.pmi.tpd.core.model.euceg.SubmitterEntity;
import com.pmi.tpd.core.model.euceg.SubmitterRevision;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public interface ISubmitterStore {

  /**
   * @param pageRequest
   * @return
   */
  Page<SubmitterEntity> findAll(Pageable pageRequest);

  /**
   * @return Returns the number of stored submitters.
   */
  long count();

  /**
   * @param submitterId
   * @return
   */
  @Nonnull
  Boolean exists(@Nonnull String submitterId);

  /**
   * @param submitterId
   * @return
   */
  @Nonnull
  SubmitterEntity get(@Nonnull String submitterId);

  /**
   * @param submitterId
   * @return
   */
  @Nullable
  SubmitterEntity find(@Nonnull final String submitterId);

  /**
   * find submitter revisions
   *
   * @param submitterId
   *                    submitter ID
   * @param pageRequest
   * @return
   */
  Page<SubmitterRevision> findRevisions(@Nonnull String submitterId, @Nonnull Pageable pageRequest);

  SubmitterRevision getCurrentRevision(@Nonnull String submitterId);

  /**
   * compare two revisions of a submitter
   *
   * @param submitterId
   *                         submitter Id
   * @param originalRevision
   *                         original revision
   * @param revisedRevision
   *                         revised revision
   * @return
   */
  SubmitterDifference compare(@Nonnull String submitterId, Integer originalRevision, Integer revisedRevision)
      throws IOException;

  /**
   * @param submitter
   * @return
   */
  @Nonnull
  SubmitterEntity create(@Nonnull SubmitterEntity submitter);

  /**
   * @param submitter
   */
  SubmitterEntity save(@Nonnull SubmitterEntity submitter);

  /**
   * @param submitter
   */
  void remove(@Nonnull SubmitterEntity submitter);

  /**
   * Deletes the submitter with the given id.
   *
   * @param id
   *           must not be {@literal null}.
   * @throws IllegalArgumentException
   *                                        in case the given {@code id} is
   *                                        {@literal null}
   * @throws EmptyResultDataAccessException
   *                                        if not submitter with {@code id}
   *                                        exists!
   */
  void remove(@Nonnull String submitterId);

}
