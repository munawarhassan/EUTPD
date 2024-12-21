package com.pmi.tpd.euceg.core.importer;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.pmi.tpd.euceg.core.util.validation.ValidationResult;

/**
 * @author Christophe Friederich
 * @since 1.0
 * @param <T>
 */
public class ImportResultImpl<T> implements IImporterResult<T> {

  private final List<T> results;

  private final ValidationResult validationResult;

  public ImportResultImpl(@Nonnull final ValidationResult validationResult) {
    this(Collections.<T>emptyList(), validationResult);
  }

  public ImportResultImpl(@Nonnull final List<T> results, @Nonnull final ValidationResult validationResult) {
    this.results = checkNotNull(results, "results");
    this.validationResult = checkNotNull(validationResult, "validationResult");
  }

  @Override
  @Nonnull
  public List<T> getResults() {
    return results;
  }

  @Override
  @Nonnull
  public ValidationResult getValidationResult() {
    return validationResult;
  }
}
