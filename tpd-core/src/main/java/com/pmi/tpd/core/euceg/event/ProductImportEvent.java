package com.pmi.tpd.core.euceg.event;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.audit.AuditEntryBuilder;
import com.pmi.tpd.api.audit.AuditEntryConverter;
import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.api.audit.annotation.Audited;
import com.pmi.tpd.api.event.BaseEvent;
import com.pmi.tpd.euceg.api.ProductType;

/**
 * Raised when some products are imported.
 *
 * @since 3.0
 * @author devacfr
 */
@Audited(converter = ProductImportEvent.AuditConverter.class, priority = Priority.HIGH, channels = {
    EucegChannels.EUCEG, EucegChannels.PRODUCT })
public class ProductImportEvent extends BaseEvent {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /** */
  private final String filename;

  /** */
  private final ProductType productType;

  /** */
  private final List<String> productNumbers;

  /**
   * Default constructor.
   *
   * @param source
   *                      The object on which the Event initially occurred.
   * @param productNumber
   *                      the productNumber.
   * @param productType
   *                      the product type
   */
  public ProductImportEvent(@Nonnull final Object source, @Nonnull final String filename,
      @Nonnull final ProductType productType, @Nonnull final List<String> productNumbers) {
    super(source);
    this.filename = checkNotNull(filename, "filename");
    this.productNumbers = checkNotNull(productNumbers, "productNumbers");
    this.productType = checkNotNull(productType, "productType");
  }

  @Nonnull
  public String getFilename() {
    return filename;
  }

  @Nonnull
  public List<String> getProductNumber() {
    return productNumbers;
  }

  @Nonnull
  public ProductType getProductType() {
    return productType;
  }

  public static class AuditConverter implements AuditEntryConverter<ProductImportEvent> {

    @Override
    public IAuditEntry convert(final ProductImportEvent event, final AuditEntryBuilder builder) {
      return builder
          .details(ImmutableMap.<String, String>builder()
              .put("filename", event.filename)
              .put("productNumbers", event.productNumbers.stream().collect(Collectors.joining(",")))
              .put("productType", event.productType.toString())
              .build())
          .build();
    }

  }

}