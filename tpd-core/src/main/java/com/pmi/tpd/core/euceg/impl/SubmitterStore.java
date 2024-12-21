package com.pmi.tpd.core.euceg.impl;

import static com.pmi.tpd.api.util.Assert.checkHasText;
import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.JAXBException;

import org.apache.commons.collections.CollectionUtils;
import org.eu.ceg.Submitter;
import org.eu.ceg.SubmitterDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revision;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.euceg.spi.ISubmitterRepository;
import com.pmi.tpd.core.euceg.spi.ISubmitterStore;
import com.pmi.tpd.core.model.euceg.QSubmitterEntity;
import com.pmi.tpd.core.model.euceg.SubmitterDifference;
import com.pmi.tpd.core.model.euceg.SubmitterEntity;
import com.pmi.tpd.core.model.euceg.SubmitterRevision;
import com.pmi.tpd.database.hibernate.HibernateUtils;
import com.pmi.tpd.euceg.api.entity.SubmitterStatus;
import com.pmi.tpd.euceg.core.ValidationHelper;
import com.pmi.tpd.euceg.core.support.EucegXmlDiff;
import com.pmi.tpd.euceg.core.util.validation.ValidationResult;

@Singleton
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class SubmitterStore implements ISubmitterStore {

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(SubmitterStore.class);

  private final ISubmitterRepository repository;

  @Inject
  public SubmitterStore(final ISubmitterRepository repository) {
    this.repository = checkNotNull(repository, "repository");
  }

  public QSubmitterEntity entity() {
    return this.repository.entity();
  }

  @Override
  public Page<SubmitterEntity> findAll(final Pageable pageRequest) {
    return this.repository.findAll(pageRequest);
  }

  @Override
  public long count() {
    return this.repository.count();
  }

  @Nonnull
  @Override
  public Boolean exists(@Nonnull final String submitterId) {
    return this.repository.existsById(checkHasText(submitterId, "submitterId"));
  }

  @Nonnull
  @Override
  public SubmitterEntity get(@Nonnull final String submitterId) {
    return HibernateUtils.initialize(this.repository.getById(checkHasText(submitterId, "submitterId")));
  }

  @Override
  public SubmitterEntity find(@Nonnull final String submitterId) {
    return HibernateUtils.initialize(this.repository.findById(checkHasText(submitterId, "submitterId")))
        .orElse(null);
  }

  @Override
  public Page<SubmitterRevision> findRevisions(@Nonnull final String submitterId,
      @Nonnull final Pageable pageRequest) {

    return this.repository.findRevisions(submitterId, pageRequest).map(SubmitterRevision::fromRevision);
  }

  @Override
  public SubmitterDifference compare(@Nonnull final String submitterId,
      final Integer originalRevision,
      final Integer revisedRevision) throws IOException {

    final Revision<Integer, SubmitterEntity> original = this.repository.findRevision(submitterId, originalRevision)
        .orElseThrow();

    final Revision<Integer, SubmitterEntity> revised = this.repository.findRevision(submitterId, revisedRevision)
        .orElseThrow();

    final EucegXmlDiff submitterDiff = new EucegXmlDiff(submitterId,
        EucegXmlDiff.getXml(original.getEntity().getSubmitter(), Submitter.class, true),
        EucegXmlDiff.getXml(revised.getEntity().getSubmitter(), Submitter.class, true));

    final EucegXmlDiff detailsDiff = new EucegXmlDiff(submitterId,
        EucegXmlDiff.getXml(original.getEntity().getSubmitterDetails(), SubmitterDetails.class, false),
        EucegXmlDiff.getXml(revised.getEntity().getSubmitterDetails(), SubmitterDetails.class, false));

    return SubmitterDifference.builder()
        .submitterId(submitterId)
        .originalRevision(originalRevision)
        .revisedRevision(revisedRevision)
        .submitterChangeType(submitterDiff.result().getChange())
        .submitterPatch(submitterDiff.result().getPatch())
        .submitterDetailsChangeType(detailsDiff.result().getChange())
        .submitterDetailsPatch(detailsDiff.result().getPatch())
        .build();
  }

  @Override
  public SubmitterRevision getCurrentRevision(@Nonnull final String submitterId) {
    return this.repository.findLastChangeRevision(submitterId).map(SubmitterRevision::fromRevision).orElseThrow();
  }

  @Override
  @Transactional
  public SubmitterEntity save(@Nonnull final SubmitterEntity submitter) {
    checkNotNull(submitter, "submitter");
    return repository.save(normalize(submitter, submitter.getSubmitterId()));
  }

  @Nonnull
  @Override
  @Transactional
  public SubmitterEntity create(@Nonnull final SubmitterEntity submitter) {
    checkNotNull(submitter, "submitter");
    Assert.state(!Strings.isNullOrEmpty(submitter.getSubmitterId()), "submitterId can not be null or empty");
    final int id = Integer.parseInt(submitter.getSubmitterId());
    final String submitterId = String.format("%05d", id);
    if (this.repository.existsById(submitterId)) {
      throw new DuplicateKeyException("the submitter " + submitter.getSubmitterId() + " already exists");
    } else {
      return repository.save(normalize(submitter, submitterId));
    }
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public void remove(@Nonnull final SubmitterEntity submitter) {
    checkNotNull(submitter, "submitter");
    this.repository.delete(submitter);
  }

  @Override
  @Transactional
  public void remove(@Nonnull final String submitterId) {
    this.repository.deleteById(Assert.checkHasText(submitterId, "submitterId"));
  }

  protected SubmitterEntity normalize(@Nonnull final SubmitterEntity entity, @Nonnull final String submitterId) {
    final Submitter submitter = entity.getSubmitter();
    submitter.withSubmitterID(submitterId)
        .withHasParent(submitter.getParent() != null)
        .withHasEnterer(submitter.getEnterer() != null)
        .withHasAffiliates(submitter.getAffiliates() != null
            && CollectionUtils.isNotEmpty(submitter.getAffiliates().getAffiliate()));

    final SubmitterDetails details = entity.getSubmitterDetails();
    details.withHasVatNumber(!Strings.isNullOrEmpty(details.getVatNumber()));
    return updateStatus(entity.copy()
        .submitterId(submitterId)
        .name(entity.getSubmitterDetails().getName())
        .submitter(submitter)
        .details(details)
        .build());
  }

  private SubmitterEntity updateStatus(final SubmitterEntity entity) {
    if (SubmitterStatus.SENT.equals(entity.getStatus())) {
      return entity;
    }
    final ValidationResult result = new ValidationResult();
    SubmitterStatus oldStatus = entity.getStatus();
    if (oldStatus == null) {
      oldStatus = SubmitterStatus.DRAFT;
    }
    if (oldStatus == SubmitterStatus.VALID) {
      oldStatus = SubmitterStatus.DRAFT;
    }
    final SubmitterStatus status = validate(entity.getSubmitterDetails(), result) ? SubmitterStatus.VALID
        : oldStatus;
    return entity.copy().status(status).build();
  }

  private boolean validate(@Nonnull final SubmitterDetails detail, @Nonnull final ValidationResult result) {
    checkNotNull(detail, "detail");
    checkNotNull(result, "result");
    try {
      return ValidationHelper.validateSubmission(detail, result);
    } catch (final JAXBException e) {
      LOGGER.warn(e.getMessage(), e);
      return false;
    }

  }
}
