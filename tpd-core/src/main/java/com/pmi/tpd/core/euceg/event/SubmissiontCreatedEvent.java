package com.pmi.tpd.core.euceg.event;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;

import org.eu.ceg.SubmissionTypeEnum;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.audit.AuditEntryBuilder;
import com.pmi.tpd.api.audit.AuditEntryConverter;
import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.api.audit.annotation.Audited;
import com.pmi.tpd.api.event.BaseEvent;
import com.pmi.tpd.euceg.api.ProductType;

/**
 * Raised when a submission is created.
 *
 * @since 3.0
 * @author devacfr
 */
@Audited(converter = SubmissiontCreatedEvent.AuditConverter.class, priority = Priority.HIGH, channels = {
    EucegChannels.EUCEG, EucegChannels.SUBMISSION })
public class SubmissiontCreatedEvent extends BaseEvent {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final long submissionId;

  /** */
  private final String productId;

  /** */
  private final SubmissionTypeEnum submissionType;

  /** */
  private final String productNumber;

  /** */
  private final ProductType productType;

  /**
   * Default constructor.
   *
   * @param source
   *                       The object on which the Event initially occurred.
   * @param productId
   *                       the productId
   * @param submissionType
   *                       the submission type
   * @param productType
   *                       the product type
   * @param productNumber
   *                       the productNumber.
   */
  public SubmissiontCreatedEvent(@Nonnull final Object source, final long submissionId,
      @Nonnull final String productId, @Nonnull final SubmissionTypeEnum submissionType,
      @Nonnull final String productNumber, @Nonnull final ProductType productType) {
    super(source);
    this.submissionId = submissionId;
    this.productId = checkNotNull(productId, "productId");
    this.submissionType = checkNotNull(submissionType, "submissionType");
    this.productNumber = checkNotNull(productNumber, "productNumber");
    this.productType = checkNotNull(productType, "productType");
  }

  public long getSubmissionId() {
    return submissionId;
  }

  @Nonnull
  public String getProductId() {
    return productId;
  }

  @Nonnull
  public SubmissionTypeEnum getSubmissionType() {
    return submissionType;
  }

  @Nonnull
  public String getProductNumber() {
    return productNumber;
  }

  @Nonnull
  public ProductType getProductType() {
    return productType;
  }

  public static class AuditConverter implements AuditEntryConverter<SubmissiontCreatedEvent> {

    @Override
    public IAuditEntry convert(final SubmissiontCreatedEvent event, final AuditEntryBuilder builder) {
      return builder
          .details(ImmutableMap.<String, String>builder()
              .put("submissionId", Long.toString(event.submissionId))
              .put("productId", event.productId)
              .put("submissionType",
                  com.pmi.tpd.euceg.core.refs.SubmissionTypeEnum.fromValue(event.submissionType.value())
                      .map(e -> e.getDescription())
                      .orElse(""))
              .put("productNumber", event.productNumber)
              .put("productType", event.productType.toString())
              .build())
          .build();
    }

  }

}